package com.rauio.smartdangjian.controller.factory;

import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.spec.BlockType;

/**
 * Static factory for content test data — produces ChapterContentBlock and ContentBlockResponse
 * instances, lists, and JSON helpers.
 */
public final class ContentTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private ContentTestDataFactory() {}

    // ── ChapterContentBlock builders ──────────────────────────────────────

    public static ChapterContentBlock createContentBlock(Long id, Long chapterId, BlockType blockType) {
        return ChapterContentBlock.builder()
                .id(id)
                .chapterId(chapterId)
                .orderIndex(0)
                .blockType(blockType)
                .textContent("测试内容")
                .resourceId(id)
                .caption("说明-" + id)
                .build();
    }

    public static ChapterContentBlock createContentBlock(Long id, Long chapterId, BlockType blockType, int orderIndex) {
        return ChapterContentBlock.builder()
                .id(id)
                .chapterId(chapterId)
                .orderIndex(orderIndex)
                .blockType(blockType)
                .textContent("测试内容-" + orderIndex)
                .resourceId(id)
                .caption("说明-" + id)
                .build();
    }

    // ── ContentBlockResponse builders (uses ReflectionTestUtils for field access) ──

    public static ContentBlockResponse createContentBlockResponse(
            Long parentId, BlockType blockType, String textContent) {
        ContentBlockResponse vo = new ContentBlockResponse();
        ReflectionTestUtils.setField(vo, "parentId", parentId);
        ReflectionTestUtils.setField(vo, "blockType", blockType);
        ReflectionTestUtils.setField(vo, "textContent", textContent);
        ReflectionTestUtils.setField(vo, "resourceId", parentId);
        ReflectionTestUtils.setField(vo, "caption", "说明");
        return vo;
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
