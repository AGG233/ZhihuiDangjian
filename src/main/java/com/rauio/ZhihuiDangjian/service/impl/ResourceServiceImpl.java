package com.rauio.ZhihuiDangjian.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.rauio.ZhihuiDangjian.dao.ContentBlockDao;
import com.rauio.ZhihuiDangjian.dao.ResourceMetaDao;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.pojo.ContentBlock;
import com.rauio.ZhihuiDangjian.pojo.ResourceMeta;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.convertor.ContentBlockConvertor;
import com.rauio.ZhihuiDangjian.pojo.dto.ContentBlockDto;
import com.rauio.ZhihuiDangjian.service.ResourceService;
import com.rauio.ZhihuiDangjian.service.UserService;
import com.rauio.ZhihuiDangjian.utils.CosUtils;
import com.rauio.ZhihuiDangjian.utils.HashUtil;
import com.rauio.ZhihuiDangjian.utils.MediaTypeUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.rauio.ZhihuiDangjian.constants.ErrorConstants.ARGS_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class ResourceServiceImpl implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final ResourceMetaDao       metaDao;
    private final ContentBlockDao       blockDao;
    private final COSClient             cosClient;
    private final UserService           userService;
    private final ContentBlockConvertor blockConvertor;

    @Value("${tencent.cloud.cos.bucket}")
    private String  bucketName;
    @Value("${tencent.cloud.cos.presigned-url-expire-time}")
    private long    expireTimeInSeconds;


    /**
     * @param file 上传文件
     * @return 资源ID
     *
     */
    @Override
    @Async
    public CompletableFuture<Map<String, String>> saveFile(MultipartFile file, User user) throws IOException, NoSuchAlgorithmException {
        try {
            if (user == null) {
                throw new BusinessException(ARGS_ERROR, "用户不存在");
            }

            String mediaType = MediaTypeUtil.detect(file);

            String hash;
            try (InputStream hashInputStream = file.getInputStream()) {
                hash = HashUtil.calculateSHA256(hashInputStream);
            }

            /*
             * 上传到COS
             * */
            String originalFilename = file.getOriginalFilename();
            String extension = "." + FilenameUtils.getExtension(originalFilename);
            String objectKey = mediaType + hash + extension;

            ObjectMetadata metadata = CosUtils.setObjectMetadata(user, file);
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    objectKey,
                    inputStream,
                    metadata
            );
            cosClient.putObject(putObjectRequest);
            metaDao.save(ResourceMeta.builder()
                    .uploaderId(user.getId())
                    .originalName(originalFilename)
                    .hash(hash)
                    .build()
            );

            Map<String, String> result = new HashMap<>();
            ResourceMeta meta = metaDao.findByHash(hash);
            result.put("resource_id", meta.getId().toString());
            result.put("resource_hash", meta.getHash());
            result.put("resource_original_name", meta.getOriginalName());


            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(4003,"文件上传失败");
        }
    }

    /**
     * @param files 上传的文件集合
     * @return 上传结果
     */
    @Override
    public Map<String, String> saveFileBatch(List<MultipartFile> files) {
        Map<String, String> result = new HashMap<>();
        files.forEach(file -> {
            try {
                CompletableFuture<Map<String, String>> map = saveFile(file, userService.getUserFromAuthentication());
                result.putAll(map.get());
            } catch (IOException | NoSuchAlgorithmException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    @Override
    public Boolean saveBlock(ContentBlockDto Dto) {
        ContentBlock block = blockConvertor.toEntity(Dto);
        return blockDao.insert(block);
    }


    @Override
    public URL getByHash(String Hash) {
        String path = metaDao.findByHash(Hash).getHash();
        try{
            Date expiration = new Date(new Date().getTime() + expireTimeInSeconds * 1000);
            return cosClient.generatePresignedUrl(bucketName, path, expiration, HttpMethodName.GET);
        }catch (Exception e){
            log.error("文件获取失败",e);
            throw new BusinessException(4001,"文件获取失败");
        }
    }

    @Override
    public URL getById(Long id) {
        String path = metaDao.findByResourceId(id).getHash();
        try{
            Date expiration = new Date(new Date().getTime() + expireTimeInSeconds * 1000);
            return cosClient.generatePresignedUrl(bucketName, path, expiration, HttpMethodName.GET);
        }catch (Exception e){
            log.error("文件获取失败",e);
            throw new BusinessException(4001,"文件获取失败");
        }
    }

    @Override
    public List<String> getBatchWithId(List<Long> idList) {
        return idList.stream()
                .map(key ->{
                    URL url = getById(key);
                    return url.toString();
                }).collect(Collectors.toList());
    }

    @Override
    public List<String> getBatchWithHash(List<String> hashList) {
       return hashList.stream()
               .map(key ->{
                   URL url = getByHash(key);
                   return url.toString();
               }).collect(Collectors.toList());
    }

    @Override
    public boolean delete(String hash) {
        Long id = metaDao.findByHash(hash).getId();
        metaDao.delete(id);
        blockDao.delete(blockDao.getByResourceId(id).getId());
        return true;
    }
    @Override
    public boolean delete(String[] keys) {
        for (String key: keys){
            delete(key);
        }
        return true;
    }
}