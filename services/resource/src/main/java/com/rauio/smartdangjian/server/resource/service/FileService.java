package com.rauio.smartdangjian.server.resource.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.constant.Constant;
import org.dromara.x.file.storage.core.presigned.GeneratePresignedUrlResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.constants.ResourceConstant;
import com.rauio.smartdangjian.server.resource.constants.ResourceErrorConstants;
import com.rauio.smartdangjian.server.resource.constants.ResourceStatusConstants;
import com.rauio.smartdangjian.server.resource.constants.ResourceTypeConstants;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.UploadFileRequest;
import com.rauio.smartdangjian.server.resource.pojo.response.FileInfoResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.FileUploadResponse;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.service.PermissionValidator;

import cn.hutool.core.date.DateUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private static final Map<String, Set<String>> ALLOWED_EXTENSIONS_BY_MIME = Map.of(
            "image/jpeg", Set.of(".jpg", ".jpeg"),
            "image/png", Set.of(".png"),
            "image/gif", Set.of(".gif"),
            "image/webp", Set.of(".webp"),
            "video/mp4", Set.of(".mp4"),
            "video/webm", Set.of(".webm"),
            "application/pdf", Set.of(".pdf"));

    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final ResourceMetaService resourceMetaService;
    private final PermissionValidator permissionValidator;

    @Value("${app.storage.local-root:./uploads}")
    private String localStorageRoot;

    @CircuitBreaker(name = "cosService", fallbackMethod = "uploadFallback")
    public FileUploadResponse upload(UploadFileRequest request) {
        validateUploadRequest(request);
        String extension = extractExtension(request.getFileName());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String path = resolvePath(request.getMimeType());
        String filename = uuid + extension;
        String objectKey = path + filename;

        ResourceMetaCreateRequest createRequest = new ResourceMetaCreateRequest();
        createRequest.setUploaderId(request.getUserId() != null ? request.getUserId() : userService.getCurrentUserId());
        createRequest.setOriginalName(request.getFileName());
        createRequest.setHash(uuid);
        createRequest.setObjectKey(objectKey);
        createRequest.setResourceType(detectResourceType(request.getMimeType()));
        createRequest.setStatus(ResourceStatusConstants.UPLOADING);
        ResourceMeta meta = resourceMetaService.create(createRequest);

        String uploadUrl;
        long expiration;
        try {
            GeneratePresignedUrlResult urlResult = fileStorageService
                    .generatePresignedUrl()
                    .setPlatform(ResourceConstant.COS_PLATFORM)
                    .setPath(path)
                    .setFilename(filename)
                    .setMethod(Constant.GeneratePresignedUrl.Method.PUT)
                    .setExpiration(DateUtil.offsetMinute(new Date(), 10))
                    .putHeaders(Constant.Metadata.CONTENT_TYPE, request.getMimeType())
                    .putUserMetadata("resourceId", String.valueOf(meta.getId()))
                    .generatePresignedUrl();
            uploadUrl = urlResult.getUrl();
            expiration = System.currentTimeMillis() + ResourceConstant.COS_KEY_EXPIRATION;
        } catch (Exception e) {
            log.warn("COS 预签名 URL 生成失败，回退到服务器中转上传", e);
            uploadUrl = "/api/resource/files/upload/callback/" + meta.getId();
            expiration = -1L;
        }

        return FileUploadResponse.builder()
                .resourceId(String.valueOf(meta.getId()))
                .uploadUrl(uploadUrl)
                .objectKey(objectKey)
                .expiration(expiration)
                .build();
    }

    @CircuitBreaker(name = "cosService", fallbackMethod = "confirmUploadFallback")
    public ResourceMeta confirmUpload(Long resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        permissionValidator.requireResourceAccess(meta.getUploaderId(), "无权确认该文件");
        if (meta.getStatus() != null && meta.getStatus() == ResourceStatusConstants.PUBLIC) {
            return meta;
        }

        boolean exists;
        try {
            exists = fileStorageService.exists(buildFileInfo(meta.getObjectKey()));
        } catch (Exception e) {
            log.warn("COS 文件检查失败，尝试检查本地文件", e);
            exists = Files.exists(resolveLocalPath(meta.getObjectKey()));
        }

        if (!exists) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_NOT_FOUND, "文件尚未上传到存储服务器，请先上传");
        }
        resourceMetaService.markPublic(resourceId);
        meta.setStatus(ResourceStatusConstants.PUBLIC);
        return meta;
    }

    public void handleUploadCallback(Long resourceId, InputStream inputStream) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        permissionValidator.requireResourceAccess(meta.getUploaderId(), "无权上传该文件");
        try {
            Path filePath = resolveLocalPath(meta.getObjectKey());
            Files.createDirectories(filePath.getParent());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            log.error("本地文件保存失败，resourceId={}", resourceId, e);
            throw new BusinessException(ResourceErrorConstants.RESOURCE_CREATE_FAILED, "文件保存失败");
        }
    }

    @CircuitBreaker(name = "cosService", fallbackMethod = "getFileInfoFallback")
    public FileInfoResponse getFileInfo(Long resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        return buildFileInfoResponse(meta);
    }

    @CircuitBreaker(name = "cosService", fallbackMethod = "getFileInfoFallback")
    public FileInfoResponse getFileInfoByHash(String hash) {
        ResourceMeta meta = resourceMetaService.getByHash(hash);
        return buildFileInfoResponse(meta);
    }

    private FileInfoResponse buildFileInfoResponse(ResourceMeta meta) {
        String downloadUrl = generateDownloadUrl(meta.getObjectKey());
        return FileInfoResponse.builder()
                .resourceId(String.valueOf(meta.getId()))
                .originalName(meta.getOriginalName())
                .hash(meta.getHash())
                .objectKey(meta.getObjectKey())
                .resourceType(meta.getResourceType())
                .status(meta.getStatus())
                .downloadUrl(downloadUrl)
                .build();
    }

    public String getDownloadUrl(Long resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        return generateDownloadUrl(meta.getObjectKey());
    }

    @CircuitBreaker(name = "cosService", fallbackMethod = "deleteFallback")
    public void delete(Long resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        permissionValidator.requireResourceAccess(meta.getUploaderId(), "无权删除该文件");
        try {
            FileInfo fileInfo = buildFileInfo(meta.getObjectKey());
            fileStorageService.delete(fileInfo);
        } catch (Exception e) {
            log.error("删除COS文件失败，元数据未删除: resourceId={}, objectKey={}", resourceId, meta.getObjectKey(), e);
            throw new BusinessException(ResourceErrorConstants.RESOURCE_DELETE_FAILED, "文件删除失败，请稍后重试");
        }
        resourceMetaService.delete(resourceId);
    }

    public List<String> getBatchByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> distinctIds = ids.stream().distinct().collect(Collectors.toList());
        List<ResourceMeta> metas = resourceMetaService.listByIds(distinctIds);
        Map<Long, String> urlMap = metas.stream()
                .collect(Collectors.toMap(ResourceMeta::getId, meta -> generateDownloadUrl(meta.getObjectKey())));
        return ids.stream().map(id -> urlMap.get(id)).collect(Collectors.toList());
    }

    public List<String> getBatchByHashes(List<String> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> distinctHashes = hashes.stream().distinct().collect(Collectors.toList());
        List<ResourceMeta> metas = resourceMetaService.list(
                new LambdaQueryWrapper<ResourceMeta>().in(ResourceMeta::getHash, distinctHashes));
        Map<String, String> urlMap = metas.stream()
                .collect(Collectors.toMap(ResourceMeta::getHash, meta -> generateDownloadUrl(meta.getObjectKey())));
        return hashes.stream().map(hash -> urlMap.get(hash)).collect(Collectors.toList());
    }

    public String getByHash(String hash) {
        ResourceMeta meta = resourceMetaService.getByHash(hash);
        return generateDownloadUrl(meta.getObjectKey());
    }

    private String generateDownloadUrl(String objectKey) {
        String path = extractPath(objectKey);
        String filename = extractFilename(objectKey);

        try {
            GeneratePresignedUrlResult result = fileStorageService
                    .generatePresignedUrl()
                    .setPlatform(ResourceConstant.COS_PLATFORM)
                    .setPath(path)
                    .setFilename(filename)
                    .setMethod(Constant.GeneratePresignedUrl.Method.GET)
                    .setExpiration(DateUtil.offsetMinute(new Date(), 10))
                    .generatePresignedUrl();

            return result.getUrl();
        } catch (Exception e) {
            log.error("生成 COS 预签名下载 URL 失败，objectKey={}", objectKey, e);
            throw new BusinessException(ResourceErrorConstants.RESOURCE_NOT_FOUND, "文件服务暂不可用，请稍后重试");
        }
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
        return lastDot >= 0 ? fileName.substring(lastDot).toLowerCase(Locale.ROOT) : "";
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

    private void validateUploadRequest(UploadFileRequest request) {
        String mimeType = normalizeMimeType(request.getMimeType());
        String extension = extractExtension(request.getFileName());
        Set<String> allowedExtensions = ALLOWED_EXTENSIONS_BY_MIME.get(mimeType);
        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_INVALID_FILE, "不支持的文件类型");
        }
    }

    private String normalizeMimeType(String mimeType) {
        return mimeType == null ? "" : mimeType.trim().toLowerCase(Locale.ROOT);
    }

    private Path resolveLocalPath(String objectKey) {
        try {
            Path root = Path.of(localStorageRoot).toAbsolutePath().normalize();
            Path resolved = root.resolve(objectKey).normalize();
            if (!resolved.startsWith(root)) {
                throw new BusinessException(ResourceErrorConstants.RESOURCE_INVALID_FILE, "非法文件路径");
            }
            return resolved;
        } catch (InvalidPathException e) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_INVALID_FILE, "非法文件路径");
        }
    }

    /**
     * COS 上传熔断降级回退方法。
     */
    @SuppressWarnings("unused")
    private FileUploadResponse uploadFallback(UploadFileRequest request, Throwable t) {
        log.error("COS 服务熔断降级，文件上传失败: fileName={}", request.getFileName(), t);
        throw new BusinessException(ResourceErrorConstants.RESOURCE_SERVICE_UNAVAILABLE, "文件存储服务暂时不可用，请稍后重试");
    }

    /**
     * COS 确认上传熔断降级回退方法。
     */
    @SuppressWarnings("unused")
    private ResourceMeta confirmUploadFallback(Long resourceId, Throwable t) {
        log.error("COS 服务熔断降级，确认上传失败: resourceId={}", resourceId, t);
        throw new BusinessException(ResourceErrorConstants.RESOURCE_SERVICE_UNAVAILABLE, "文件存储服务暂时不可用，请稍后重试");
    }

    /**
     * COS 文件查询熔断降级回退方法。
     */
    @SuppressWarnings("unused")
    private FileInfoResponse getFileInfoFallback(Long resourceId, Throwable t) {
        log.error("COS 服务熔断降级，文件查询失败: resourceId={}", resourceId, t);
        throw new BusinessException(ResourceErrorConstants.RESOURCE_SERVICE_UNAVAILABLE, "文件存储服务暂时不可用，请稍后重试");
    }

    /**
     * COS 文件查询（by hash）熔断降级回退方法。
     */
    @SuppressWarnings("unused")
    private FileInfoResponse getFileInfoFallback(String hash, Throwable t) {
        log.error("COS 服务熔断降级，文件查询失败: hash={}", hash, t);
        throw new BusinessException(ResourceErrorConstants.RESOURCE_SERVICE_UNAVAILABLE, "文件存储服务暂时不可用，请稍后重试");
    }

    /**
     * COS 文件删除熔断降级回退方法。
     */
    @SuppressWarnings("unused")
    private void deleteFallback(Long resourceId, Throwable t) {
        log.error("COS 服务熔断降级，文件删除失败: resourceId={}", resourceId, t);
        throw new BusinessException(ResourceErrorConstants.RESOURCE_SERVICE_UNAVAILABLE, "文件存储服务暂时不可用，请稍后重试");
    }
}
