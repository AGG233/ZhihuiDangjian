package com.rauio.smartdangjian.controller.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaUpdateRequest;

/**
 * Static factory for resource meta test data — produces ResourceMetaCreateRequest,
 * ResourceMetaUpdateRequest, and a JSON helper.
 */
public final class ResourceMetaTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ResourceMetaTestDataFactory() {}

    // ── ResourceMetaCreateRequest ───────────────────────────────────

    public static ResourceMetaCreateRequest createResourceMetaCreateRequest() {
        ResourceMetaCreateRequest req = new ResourceMetaCreateRequest();
        req.setUploaderId("admin1");
        req.setOriginalName("test-file.png");
        req.setHash("hash-123");
        req.setObjectKey("image/hash-123.png");
        req.setResourceType(0);
        req.setStatus(1);
        return req;
    }

    public static ResourceMetaCreateRequest createResourceMetaCreateRequest(String hash) {
        ResourceMetaCreateRequest req = new ResourceMetaCreateRequest();
        req.setUploaderId("admin1");
        req.setOriginalName("test-file.png");
        req.setHash(hash);
        req.setObjectKey("image/" + hash + ".png");
        req.setResourceType(0);
        req.setStatus(1);
        return req;
    }

    // ── ResourceMetaUpdateRequest ───────────────────────────────────

    public static ResourceMetaUpdateRequest createResourceMetaUpdateRequest() {
        ResourceMetaUpdateRequest req = new ResourceMetaUpdateRequest();
        req.setOriginalName("updated-file.png");
        req.setResourceType(1);
        req.setObjectKey("image/updated-hash.png");
        req.setStatus(2);
        return req;
    }

    public static ResourceMetaUpdateRequest createEmptyUpdateRequest() {
        return new ResourceMetaUpdateRequest();
    }

    // ── JSON helper ─────────────────────────────────────────────────

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}
