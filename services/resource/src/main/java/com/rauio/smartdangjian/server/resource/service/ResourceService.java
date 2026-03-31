package com.rauio.smartdangjian.server.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import com.rauio.smartdangjian.common.utils.CosUtils;
import com.rauio.smartdangjian.common.utils.HashUtil;
import com.rauio.smartdangjian.common.utils.MediaTypeUtil;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.mapper.ContentBlockMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.ContentBlock;
import com.rauio.smartdangjian.server.resource.mapper.ResourceMetaMapper;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@Deprecated
@ConditionalOnProperty(prefix = "tencent.cloud.cos", name = "enabled", havingValue = "true")
public class ResourceService {


    private final ResourceMetaMapper    resourceMetaMapper;
    private final ContentBlockMapper    contentBlockMapper;
    private final COSClient             cosClient;
    private final UserService           userService;
    private final ContentBlockConvertor blockConvertor;
    private final TransferManager       transferManager;

    @Value("${tencent.cloud.cos.bucket}")
    private String  bucketName;
    @Value("${tencent.cloud.cos.presigned-url-expire-time}")
    private long    expireTimeInSeconds;


    /**
     * @param file 上传文件
     * @return 资源ID
     *
     */
    @Async
    public ResourceMeta create(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        User user = userService.getCurrentUser();
        return upload(file, user);
    }

    /**
     * @param files 上传的文件集合
     * @return 上传结果
     */
    public CompletableFuture<List<ResourceMeta>> createBatch(List<MultipartFile> files) {

        CompletableFuture<List<ResourceMeta>> result = new CompletableFuture<>();

        files.forEach(file -> {
            try {
                result.get().add(create(file));

            } catch (IOException | NoSuchAlgorithmException | InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    /**
     * 根据资源哈希获取预签名访问地址。
     *
     * @param Hash 资源哈希
     * @return 资源访问地址
     */
    @Cacheable(value = "resource", key = "'hash:' + #Hash")
    public URL getByHash(String Hash) {
        String path = resourceMetaMapper.selectOne(new LambdaQueryWrapper<ResourceMeta>()
                .eq(ResourceMeta::getHash, Hash)).getHash();
        try{
            Date expiration = new Date(new Date().getTime() + expireTimeInSeconds * 1000);
            return cosClient.generatePresignedUrl(bucketName, path, expiration, HttpMethodName.GET);
        }catch (Exception e){
            log.error("文件获取失败",e);
            throw new BusinessException(4001,"文件获取失败");
        }
    }

    /**
     * 根据资源 ID 获取预签名访问地址。
     *
     * @param id 资源 ID
     * @return 资源访问地址
     */
    @Cacheable(value = "resource", key = "'id:' + #id")
    public URL getById(String id) {
        String path = resourceMetaMapper.selectById(id).getHash();
        try{
            Date expiration = new Date(new Date().getTime() + expireTimeInSeconds * 1000);
            return cosClient.generatePresignedUrl(bucketName, path, expiration, HttpMethodName.GET);
        }catch (Exception e){
            log.error("文件获取失败",e);
            throw new BusinessException(4001,"文件获取失败");
        }
    }

    /**
     * 批量根据资源 ID 获取访问地址。
     *
     * @param idList 资源 ID 列表
     * @return 访问地址列表
     */
    public List<String> getByIds(List<String> idList) {
        return idList.stream()
                .map(key ->{
                    URL url = getById(key);
                    return url.toString();
                }).collect(Collectors.toList());
    }

    /**
     * 批量根据资源哈希获取访问地址。
     *
     * @param hashList 资源哈希列表
     * @return 访问地址列表
     */
    public List<String> getByHashes(List<String> hashList) {
       return hashList.stream()
               .map(key ->{
                   URL url = getByHash(key);
                   return url.toString();
               }).collect(Collectors.toList());
    }

    /**
     * 根据资源哈希删除资源元数据及关联内容块。
     *
     * @param hash 资源哈希
     * @return 是否删除成功
     */
    @CacheEvict(value = "resource", allEntries = true)
    public boolean delete(String hash) {
        ResourceMeta meta = resourceMetaMapper.selectOne(new LambdaQueryWrapper<ResourceMeta>()
                .like(ResourceMeta::getHash, hash));
        String id = meta.getId();
        resourceMetaMapper.deleteById(id);
        ContentBlock block = contentBlockMapper.selectOne(new LambdaQueryWrapper<ContentBlock>()
                .eq(ContentBlock::getResourceId, id));
        if (block != null) {
            contentBlockMapper.deleteById(block.getId());
        }
        return true;
    }

    /**
     * 批量删除资源。
     *
     * @param keys 资源哈希数组
     * @return 是否删除成功
     */
    @CacheEvict(value = "resource", allEntries = true)
    public boolean delete(String[] keys) {
        for (String key: keys){
            delete(key);
        }
        return true;
    }

    public ResourceMeta upload(MultipartFile file,User user) {
        try {
            String mediaType = MediaTypeUtil.detect(file);
            String hash;

            try (InputStream hashInputStream = file.getInputStream()) {
                hash = HashUtil.calculateSHA256(hashInputStream);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "." + FilenameUtils.getExtension(originalFilename);
            String objectKey = mediaType + hash + extension;

            ObjectMetadata metadata = CosUtils.setObjectMetadata(user.getId(), file);
            InputStream inputStream = file.getInputStream();

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    objectKey,
                    inputStream,
                    metadata
            );

            ResourceMeta meta = ResourceMeta.builder()
                    .uploaderId(user.getId())
                    .originalName(originalFilename)
                    .hash(hash)
                    .build();

            if (isExist(hash)) {
                return meta;
            }
            try {
                Upload upload = transferManager.upload(putObjectRequest);
                UploadResult uploadResult = upload.waitForUploadResult();
            } catch (CosClientException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            resourceMetaMapper.insert(meta);

            return CompletableFuture.completedFuture(meta).get();

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(500,"文件上传失败");
        }
    }

    @Cacheable(value = "resource", key = "'exists:' + #hash")
    public Boolean isExist(String hash) {
        return cosClient.doesObjectExist(bucketName, hash);
    }
}
