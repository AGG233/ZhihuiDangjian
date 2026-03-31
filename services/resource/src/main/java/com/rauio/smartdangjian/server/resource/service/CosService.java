package com.rauio.smartdangjian.server.resource.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.pojo.cache.CosUploadSession;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.*;
import com.rauio.smartdangjian.server.resource.pojo.response.MultipartInitResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.MultipartPartUploadUrlResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.MultipartProgressResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.MultipartUploadedPartResponse;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.rauio.smartdangjian.server.resource.Constant.ResourceConstant.COS_KEY_EXPIRATION;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tencent.cloud.cos", name = "enabled", havingValue = "true")
public class CosService {

    private static final String SESSION_KEY_PREFIX = "upload:session:";
    private static final String UPLOAD_ID_KEY_PREFIX = "upload:id:";
    private static final String PARTS_KEY_PREFIX = "upload:parts:";
    private static final long SESSION_TTL_HOURS = 24L;
    private static final String STATUS_UPLOADING = "UPLOADING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_ABORTED = "ABORTED";

    private final COSClient cosClient;
    private final ResourceMetaService resourceMetaService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;

    @Value("${tencent.cloud.cos.bucket}")
    private String bucketName;

    public URL generateDownloadUrl(String resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, meta.getObjectKey(), HttpMethodName.GET);
        request.setExpiration(new Date(System.currentTimeMillis() + COS_KEY_EXPIRATION));
        return cosClient.generatePresignedUrl(request);
    }

    public MultipartInitResponse initMultipartUpload(InitMultipartUploadRequest request) {
        if (resourceMetaService.existsByHash(request.fileHash())) {
            ResourceMeta meta = resourceMetaService.getByHash(request.fileHash());
            return new MultipartInitResponse(true, null, meta.getObjectKey(), request.partSize(), List.of(), meta.getId());
        }

        CosUploadSession existingSession = getSessionByFileHash(request.fileHash());
        if (existingSession != null && STATUS_UPLOADING.equals(existingSession.getStatus())) {
            refreshSessionTtl(existingSession);
            return new MultipartInitResponse(
                    false,
                    existingSession.getUploadId(),
                    existingSession.getObjectKey(),
                    existingSession.getPartSize(),
                    listUploadedPartsInternal(existingSession),
                    null
            );
        }

        String objectKey = buildObjectKey(request.fileHash(), request.suffix());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(request.contentType());
        metadata.setContentLength(request.fileSize());

        InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(bucketName, objectKey, metadata)
                .withDataSizePartSize(request.fileSize(), request.partSize());
        InitiateMultipartUploadResult initiateResult = cosClient.initiateMultipartUpload(initiateRequest);
        User currentUser = userService.getCurrentUser();

        LocalDateTime now = LocalDateTime.now();
        CosUploadSession session = CosUploadSession.builder()
                .uploadId(initiateResult.getUploadId())
                .objectKey(objectKey)
                .fileHash(request.fileHash())
                .fileName(request.fileName())
                .suffix(normalizeSuffix(request.suffix()))
                .contentType(request.contentType())
                .fileSize(request.fileSize())
                .partSize(request.partSize())
                .status(STATUS_UPLOADING)
                .uploaderId(currentUser == null ? null : currentUser.getId())
                .createdAt(now)
                .updatedAt(now)
                .build();
        saveSession(session);

        return new MultipartInitResponse(false, session.getUploadId(), session.getObjectKey(), session.getPartSize(), List.of(), null);
    }

    public MultipartPartUploadUrlResponse generatePartUploadUrl(GeneratePartUploadUrlRequest request) {
        CosUploadSession session = requireActiveSession(request.uploadId());
        GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, session.getObjectKey(), HttpMethodName.PUT);
        presignedUrlRequest.addRequestParameter("uploadId", session.getUploadId());
        presignedUrlRequest.addRequestParameter("partNumber", String.valueOf(request.partNumber()));
        presignedUrlRequest.setExpiration(new Date(System.currentTimeMillis() + COS_KEY_EXPIRATION));
        URL url = cosClient.generatePresignedUrl(presignedUrlRequest);
        refreshSessionTtl(session);
        return new MultipartPartUploadUrlResponse(session.getUploadId(), request.partNumber(), url.toString());
    }

    public Boolean recordUploadedPart(RecordUploadedPartRequest request) {
        CosUploadSession session = requireActiveSession(request.uploadId());
        redisTemplate.opsForHash().put(partsKey(session.getUploadId()), String.valueOf(request.partNumber()), request.etag());
        refreshSessionTtl(session);
        return true;
    }

    public MultipartProgressResponse getUploadProgress(MultipartUploadSessionRequest request) {
        CosUploadSession session = requireSession(request.uploadId());
        List<MultipartUploadedPartResponse> uploadedParts = listUploadedPartsInternal(session);
        refreshSessionTtl(session);
        return new MultipartProgressResponse(session.getUploadId(), session.getObjectKey(), session.getStatus(), uploadedParts);
    }

    public ResourceMeta completeMultipartUpload(MultipartUploadSessionRequest request) {
        CosUploadSession session = requireActiveSession(request.uploadId());

        if (resourceMetaService.existsByHash(session.getFileHash())) {
            ResourceMeta meta = resourceMetaService.getByHash(session.getFileHash());
            clearSession(session);
            return meta;
        }

        List<MultipartUploadedPartResponse> uploadedParts = listUploadedPartsInternal(session);
        if (uploadedParts.isEmpty()) {
            throw new BusinessException(4000, "没有可完成的分片");
        }

        List<PartETag> partETags = uploadedParts.stream()
                .sorted(Comparator.comparingInt(MultipartUploadedPartResponse::partNumber))
                .map(part -> new PartETag(part.partNumber(), part.etag()))
                .toList();

        CompleteMultipartUploadRequest completeRequest =
                new CompleteMultipartUploadRequest(bucketName, session.getObjectKey(), session.getUploadId(), partETags);
        cosClient.completeMultipartUpload(completeRequest);

        ResourceMetaCreateRequest createRequest = new ResourceMetaCreateRequest();
        createRequest.setUploaderId(StringUtils.isNotBlank(session.getUploaderId()) ? session.getUploaderId() : "0");
        createRequest.setOriginalName(session.getFileName());
        createRequest.setHash(session.getFileHash());
        createRequest.setObjectKey(session.getObjectKey());
        createRequest.setResourceType(session.getContentType());
        createRequest.setStatus(1);
        ResourceMeta meta = resourceMetaService.create(createRequest);

        session.setStatus(STATUS_COMPLETED);
        clearSession(session);
        return meta;
    }

    public Boolean abortMultipartUpload(MultipartUploadSessionRequest request) {
        CosUploadSession session = requireSession(request.uploadId());
        cosClient.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, session.getObjectKey(), session.getUploadId()));
        session.setStatus(STATUS_ABORTED);
        clearSession(session);
        return true;
    }

    private CosUploadSession getSessionByFileHash(String fileHash) {
        Object cached = redisTemplate.opsForValue().get(sessionKey(fileHash));
        return cached instanceof CosUploadSession session ? session : null;
    }

    private CosUploadSession requireSession(String uploadId) {
        Object fileHash = redisTemplate.opsForValue().get(uploadIdKey(uploadId));
        if (!(fileHash instanceof String hash) || !StringUtils.isNotBlank(hash)) {
            throw new BusinessException(4000, "上传会话不存在或已过期");
        }
        CosUploadSession session = getSessionByFileHash(hash);
        if (session == null) {
            throw new BusinessException(4000, "上传会话不存在或已过期");
        }
        return session;
    }

    private CosUploadSession requireActiveSession(String uploadId) {
        CosUploadSession session = requireSession(uploadId);
        if (!STATUS_UPLOADING.equals(session.getStatus())) {
            throw new BusinessException(4000, "上传会话不可用");
        }
        return session;
    }

    private List<MultipartUploadedPartResponse> listUploadedPartsInternal(CosUploadSession session) {
        Map<Object, Object> cachedParts = redisTemplate.opsForHash().entries(partsKey(session.getUploadId()));
        if (cachedParts == null || cachedParts.isEmpty()) {
            List<MultipartUploadedPartResponse> cosParts = listPartsFromCos(session);
            if (!cosParts.isEmpty()) {
                for (MultipartUploadedPartResponse part : cosParts) {
                    redisTemplate.opsForHash().put(partsKey(session.getUploadId()), String.valueOf(part.partNumber()), part.etag());
                }
                refreshSessionTtl(session);
            }
            return cosParts;
        }
        List<MultipartUploadedPartResponse> result = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : cachedParts.entrySet()) {
            result.add(new MultipartUploadedPartResponse(Integer.parseInt(String.valueOf(entry.getKey())), String.valueOf(entry.getValue())));
        }
        result.sort(Comparator.comparingInt(MultipartUploadedPartResponse::partNumber));
        return result;
    }

    private List<MultipartUploadedPartResponse> listPartsFromCos(CosUploadSession session) {
        PartListing partListing = cosClient.listParts(new ListPartsRequest(bucketName, session.getObjectKey(), session.getUploadId()));
        List<MultipartUploadedPartResponse> result = new ArrayList<>();
        if (partListing.getParts() == null) {
            return result;
        }
        for (PartSummary partSummary : partListing.getParts()) {
            result.add(new MultipartUploadedPartResponse(partSummary.getPartNumber(), partSummary.getETag()));
        }
        return result;
    }

    private void saveSession(CosUploadSession session) {
        redisTemplate.opsForValue().set(sessionKey(session.getFileHash()), session, SESSION_TTL_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(uploadIdKey(session.getUploadId()), session.getFileHash(), SESSION_TTL_HOURS, TimeUnit.HOURS);
    }

    private void refreshSessionTtl(CosUploadSession session) {
        session.setUpdatedAt(LocalDateTime.now());
        saveSession(session);
        redisTemplate.expire(partsKey(session.getUploadId()), SESSION_TTL_HOURS, TimeUnit.HOURS);
    }

    private void clearSession(CosUploadSession session) {
        redisTemplate.delete(sessionKey(session.getFileHash()));
        redisTemplate.delete(uploadIdKey(session.getUploadId()));
        redisTemplate.delete(partsKey(session.getUploadId()));
    }

    private String sessionKey(String fileHash) {
        return SESSION_KEY_PREFIX + fileHash;
    }

    private String uploadIdKey(String uploadId) {
        return UPLOAD_ID_KEY_PREFIX + uploadId;
    }

    private String partsKey(String uploadId) {
        return PARTS_KEY_PREFIX + uploadId;
    }

    private String buildObjectKey(String fileHash, String suffix) {
        String normalizedSuffix = normalizeSuffix(suffix);
        return "resource/" + fileHash + normalizedSuffix;
    }

    private String normalizeSuffix(String suffix) {
        if (!StringUtils.isNotBlank(suffix)) {
            return "";
        }
        return suffix.startsWith(".") ? suffix : "." + suffix;
    }
}
