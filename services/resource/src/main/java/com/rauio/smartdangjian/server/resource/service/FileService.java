package com.rauio.smartdangjian.server.resource.service;

import cn.hutool.core.date.DateUtil;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.Constant.ResourceConstant;
import com.rauio.smartdangjian.server.resource.Constant.ResourceStatusConstants;
import com.rauio.smartdangjian.server.resource.Constant.ResourceTypeConstants;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.UploadFileRequest;
import com.rauio.smartdangjian.server.resource.pojo.response.FileInfoResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.FileUploadResponse;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.constant.Constant;
import org.dromara.x.file.storage.core.presigned.GeneratePresignedUrlResult;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final FileStorageService  fileStorageService;
    private final UserService         userService;
    private final ResourceMetaService resourceMetaService;

    public FileUploadResponse upload(UploadFileRequest request) {
        String extension = extractExtension(request.getFileName());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String path = resolvePath(request.getMimeType().toString());
        String filename = uuid + extension;
        String objectKey = path + filename;

        ResourceMetaCreateRequest createRequest = new ResourceMetaCreateRequest();
        createRequest.setUploaderId(userService.getCurrentUserId());
        createRequest.setOriginalName(request.getFileName());
        createRequest.setHash(uuid);
        createRequest.setObjectKey(objectKey);
        createRequest.setResourceType(detectResourceType(request.getMimeType().toString()));
        createRequest.setStatus(ResourceStatusConstants.UPLOADING);
        ResourceMeta meta = resourceMetaService.create(createRequest);

        GeneratePresignedUrlResult urlResult = fileStorageService
                .generatePresignedUrl()
                .setPlatform(ResourceConstant.COS_PLATFORM)
                .setPath(path)
                .setFilename(filename)
                .setMethod(Constant.GeneratePresignedUrl.Method.PUT)
                .setExpiration(DateUtil.offsetMinute(new Date(), 10))
                .putHeaders(Constant.Metadata.CONTENT_TYPE, request.getMimeType().toString())
                .putUserMetadata("resourceId", meta.getId())
                .generatePresignedUrl();

        return FileUploadResponse.builder()
                .resourceId(meta.getId())
                .uploadUrl(urlResult.getUrl())
                .objectKey(objectKey)
                .expiration(System.currentTimeMillis() + ResourceConstant.COS_KEY_EXPIRATION)
                .build();
    }

    public ResourceMeta confirmUpload(String resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        if (meta.getStatus() != null && meta.getStatus() == ResourceStatusConstants.PUBLIC) {
            return meta;
        }
        meta.setStatus(ResourceStatusConstants.PUBLIC);
        resourceMetaService.updateById(meta);
        return meta;
    }

    public FileInfoResponse getFileInfo(String resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        return buildFileInfoResponse(meta);
    }

    public FileInfoResponse getFileInfoByHash(String hash) {
        ResourceMeta meta = resourceMetaService.getByHash(hash);
        return buildFileInfoResponse(meta);
    }

    private FileInfoResponse buildFileInfoResponse(ResourceMeta meta) {
        String downloadUrl = generateDownloadUrl(meta.getObjectKey());
        return FileInfoResponse.builder()
                .resourceId(meta.getId())
                .originalName(meta.getOriginalName())
                .hash(meta.getHash())
                .objectKey(meta.getObjectKey())
                .resourceType(meta.getResourceType())
                .status(meta.getStatus())
                .downloadUrl(downloadUrl)
                .build();
    }

    public String getDownloadUrl(String resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        return generateDownloadUrl(meta.getObjectKey());
    }

    public void delete(String resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        try {
            FileInfo fileInfo = buildFileInfo(meta.getObjectKey());
            fileStorageService.delete(fileInfo);
        } catch (Exception e) {
            log.warn("删除COS文件失败，可能文件已不存在: {}", meta.getObjectKey(), e);
        }
        resourceMetaService.delete(resourceId);
    }

    public List<String> getBatchByIds(List<String> ids) {
        return ids.stream()
                .map(this::getDownloadUrl)
                .collect(Collectors.toList());
    }

    public List<String> getBatchByHashes(List<String> hashes) {
        return hashes.stream()
                .map(this::getByHash)
                .collect(Collectors.toList());
    }

    public String getByHash(String hash) {
        ResourceMeta meta = resourceMetaService.getByHash(hash);
        return generateDownloadUrl(meta.getObjectKey());
    }

    private String generateDownloadUrl(String objectKey) {
        String path = extractPath(objectKey);
        String filename = extractFilename(objectKey);

        GeneratePresignedUrlResult result = fileStorageService
                .generatePresignedUrl()
                .setPlatform(ResourceConstant.COS_PLATFORM)
                .setPath(path)
                .setFilename(filename)
                .setMethod(Constant.GeneratePresignedUrl.Method.GET)
                .setExpiration(DateUtil.offsetMinute(new Date(), 10))
                .generatePresignedUrl();

        return result.getUrl();
    }

    private FileInfo buildFileInfo(String objectKey) {
        String path = extractPath(objectKey);
        String filename = extractFilename(objectKey);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform(ResourceConstant.COS_PLATFORM);
        fileInfo.setBasePath("/");
        fileInfo.setPath(path);
        fileInfo.setFilename(filename);
        return fileInfo;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot) : "";
    }

    private String extractPath(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return "";
        }
        int lastSlash = objectKey.lastIndexOf('/');
        return lastSlash >= 0 ? objectKey.substring(0, lastSlash + 1) : "";
    }

    private String extractFilename(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return "";
        }
        int lastSlash = objectKey.lastIndexOf('/');
        return lastSlash >= 0 ? objectKey.substring(lastSlash + 1) : objectKey;
    }

    private String resolvePath(String mimeType) {
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "image/";
            }
            if (mimeType.startsWith("video/")) {
                return "video/";
            }
        }
        return "resource/";
    }

    private Integer detectResourceType(String mimeType) {
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return ResourceTypeConstants.IMAGE;
            }
            if (mimeType.startsWith("video/")) {
                return ResourceTypeConstants.VIDEO;
            }
        }
        return ResourceTypeConstants.IMAGE;
    }
}
