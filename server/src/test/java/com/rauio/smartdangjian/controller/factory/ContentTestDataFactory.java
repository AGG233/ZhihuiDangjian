package com.rauio.smartdangjian.controller.factory;

import java.util.ArrayList;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.content.pojo.entity.ContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.spec.BlockType;
import com.rauio.smartdangjian.server.content.spec.ParentType;

/**
 * Static factory for content test data — produces ContentBlock and ContentBlockResponse
 * instances, lists, and JSON helpers.
 */
public final class ContentTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private ContentTestDataFactory() {}

    // ── ContentBlock builders ──────────────────────────────────────

    public static ContentBlock createContentBlock(String id, String parentId, BlockType blockType) {
        return ContentBlock.builder()
                .id(id)
                .parentId(parentId)
                .orderIndex(0)
                .parentType(ParentType.chapter)
                .blockType(blockType)
                .textContent("测试内容")
                .resourceId("res-" + id)
                .caption("说明-" + id)
                .build();
    }

    public static ContentBlock createContentBlock(String id, String parentId, BlockType blockType, int orderIndex) {
        return ContentBlock.builder()
                .id(id)
                .parentId(parentId)
                .orderIndex(orderIndex)
                .parentType(ParentType.chapter)
                .blockType(blockType)
                .textContent("测试内容-" + orderIndex)
                .resourceId("res-" + id)
                .caption("说明-" + id)
                .build();
    }

    public static ContentBlock createCarouselBlock(String id, BlockType blockType) {
        return ContentBlock.builder()
                .id(id)
                .parentId("1145141919810")
                .orderIndex(0)
                .parentType(ParentType.chapter)
                .blockType(blockType)
                .textContent("轮播图内容")
                .resourceId("res-" + id)
                .caption("轮播图" + id)
                .build();
    }

    public static List<ContentBlock> createContentBlockList(int count, String parentId) {
        List<ContentBlock> list = new ArrayList<>();
        BlockType[] types = BlockType.values();
        for (int i = 1; i <= count; i++) {
            list.add(createCarouselBlock("cb-" + String.format("%03d", i), types[i % types.length]));
        }
        return list;
    }

    // ── ContentBlockResponse builders (uses ReflectionTestUtils for field access) ──

    public static ContentBlockResponse createContentBlockResponse(
            String parentId, ParentType parentType, BlockType blockType, String textContent) {
        ContentBlockResponse vo = new ContentBlockResponse();
        ReflectionTestUtils.setField(vo, "parentId", parentId);
        ReflectionTestUtils.setField(vo, "parentType", parentType);
        ReflectionTestUtils.setField(vo, "blockType", blockType);
        ReflectionTestUtils.setField(vo, "textContent", textContent);
        ReflectionTestUtils.setField(vo, "resourceId", "res-" + parentId);
        ReflectionTestUtils.setField(vo, "caption", "说明");
        return vo;
    }

    public static ContentBlockResponse createCarouselResponse(String parentId, BlockType blockType) {
        return createContentBlockResponse(parentId, ParentType.chapter, blockType, "轮播图内容");
    }

    public static List<ContentBlockResponse> createContentBlockResponseList(int count) {
        List<ContentBlockResponse> list = new ArrayList<>();
        BlockType[] types = BlockType.values();
        for (int i = 1; i <= count; i++) {
            list.add(createCarouselResponse("1145141919810", types[i % types.length]));
        }
        return list;
    }

    // ── JSON helpers ───────────────────────────────────────────────

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    public static String listToJson(List<?> list) {
        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize list to JSON", e);
        }
    }
}
