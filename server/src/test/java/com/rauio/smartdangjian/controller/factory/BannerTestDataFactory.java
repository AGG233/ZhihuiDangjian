package com.rauio.smartdangjian.controller.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.BannerCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.BannerUpdateRequest;
import com.rauio.smartdangjian.server.resource.pojo.response.BannerResourceResponse;

/**
 * Static factory for banner test data — produces BannerCreateRequest, BannerUpdateRequest,
 * ResourceMeta, BannerResourceResponse, and a JSON helper.
 */
public final class BannerTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private BannerTestDataFactory() {
    }

    // ── BannerCreateRequest ─────────────────────────────────────────

    public static BannerCreateRequest createBannerCreateRequest() {
        return new BannerCreateRequest("resource-1", null);
    }

    public static BannerCreateRequest createBannerCreateRequestByHash() {
        return new BannerCreateRequest(null, "hash-123");
    }

    // ── BannerUpdateRequest ─────────────────────────────────────────

    public static BannerUpdateRequest createBannerUpdateRequest() {
        return new BannerUpdateRequest("resource-1", null);
    }

    public static BannerUpdateRequest createBannerUpdateRequestByHash() {
        return new BannerUpdateRequest(null, "hash-123");
    }

    // ── ResourceMeta ────────────────────────────────────────────────

    public static ResourceMeta createResourceMeta(String id) {
        return ResourceMeta.builder()
                .id(id)
                .uploaderId("admin1")
                .originalName("banner.png")
                .hash("hash-123")
                .objectKey("image/hash-123.png")
                .resourceType(0)
                .status(1)
                .build();
    }

    // ── BannerResourceResponse ──────────────────────────────────────

    public static BannerResourceResponse createBannerResourceResponse(int order) {
        return new BannerResourceResponse(
                order,
                "resource-" + order,
                "banner-" + order + ".png",
                "hash-" + order,
                "image/hash-" + order + ".png",
                0,
                1,
                "https://example.com/download/resource-" + order
        );
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
