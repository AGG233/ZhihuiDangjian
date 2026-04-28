package com.rauio.smartdangjian.controller.resource;

import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.response.FileInfoResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.FileUploadResponse;

/**
 * Static factory for file upload test data — produces FileUploadResponse,
 * FileInfoResponse, ResourceMeta with deterministic values so jsonPath
 * assertions are predictable.
 */
public final class FileTestDataFactory {

    private FileTestDataFactory() {
    }

    // ── FileUploadResponse builders ────────────────────────────────

    public static FileUploadResponse createUploadResponse() {
        return createUploadResponse("file-res-001",
                "https://cos.example.com/image/uuid-test.png?sign=abc",
                "image/uuid-test.png",
                System.currentTimeMillis() + 600_000L);
    }

    public static FileUploadResponse createUploadResponse(
            String resourceId, String uploadUrl, String objectKey, Long expiration) {
        return FileUploadResponse.builder()
                .resourceId(resourceId)
                .uploadUrl(uploadUrl)
                .objectKey(objectKey)
                .expiration(expiration)
                .build();
    }

    // ── FileInfoResponse builders ──────────────────────────────────

    public static FileInfoResponse createFileInfoResponse() {
        return FileInfoResponse.builder()
                .resourceId("file-res-001")
                .originalName("test.png")
                .hash("abc123def456")
                .objectKey("image/uuid-test.png")
                .resourceType(0) // IMAGE
                .status(1)       // PUBLIC
                .downloadUrl("https://cos.example.com/image/uuid-test.png?sign=xyz")
                .size(1048576L)
                .build();
    }

    public static FileInfoResponse createFileInfoResponse(
            String resourceId, String originalName, String hash,
            String objectKey, Integer resourceType, Integer status,
            String downloadUrl, Long size) {
        return FileInfoResponse.builder()
                .resourceId(resourceId)
                .originalName(originalName)
                .hash(hash)
                .objectKey(objectKey)
                .resourceType(resourceType)
                .status(status)
                .downloadUrl(downloadUrl)
                .size(size)
                .build();
    }

    // ── ResourceMeta builders ──────────────────────────────────────

    public static ResourceMeta createUploadingResourceMeta() {
        return createResourceMeta("file-res-001", 0); // UPLOADING
    }

    public static ResourceMeta createPublicResourceMeta() {
        return createResourceMeta("file-res-001", 1); // PUBLIC
    }

    public static ResourceMeta createResourceMeta(String id, Integer status) {
        return ResourceMeta.builder()
                .id(id)
                .uploaderId("user-001")
                .originalName("test.png")
                .hash("abc123def456")
                .objectKey("image/uuid-test.png")
                .resourceType(0) // IMAGE
                .status(status)
                .build();
    }
}
