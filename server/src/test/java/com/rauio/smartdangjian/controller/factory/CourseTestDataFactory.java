package com.rauio.smartdangjian.controller.factory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.content.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.content.pojo.response.PageResponse;
import com.rauio.smartdangjian.server.content.spec.BlockType;

/**
 * Static factory for course/chapter test data — produces CourseRequest, CourseResponse,
 * ChapterDto, ChapterVO, PageResponse, Course, and JSON helpers.
 * All IDs are deterministic strings so jsonPath assertions are predictable.
 */
public final class CourseTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private CourseTestDataFactory() {}

    // ── CourseRequest ──────────────────────────────────────────────────

    public static CourseRequest createCourseRequest() {
        return CourseRequest.builder()
                .title("test-course")
                .description("test-description")
                .categoryId("cat-1")
                .difficulty("easy")
                .estimatedDuration(60)
                .isPublished(true)
                .build();
    }

    // ── CourseResponse ───────────────────────────────────────────────────

    public static CourseResponse createCourseResponse(String id) {
        return CourseResponse.builder()
                .id(id)
                .title("test-course")
                .description("test-description")
                .categoryId("cat-1")
                .difficulty("easy")
                .estimatedDuration(60)
                .enrollmentCount(0)
                .averageRating(BigDecimal.valueOf(5.0))
                .publishedAt(LocalDateTime.now())
                .creatorId("admin1")
                .build();
    }

    // ── ChapterDto ─────────────────────────────────────────────────

    public static ChapterDto createChapterDto() {
        return ChapterDto.builder()
                .courseId("course-1")
                .title("test-chapter")
                .description("test-chapter-description")
                .duration(1800)
                .orderIndex(1)
                .isOptional(false)
                .chapterStatus("published")
                .content(List.of(createContentBlockDto()))
                .build();
    }

    // ── ChapterVO ──────────────────────────────────────────────────

    public static ChapterVO createChapterVO(String id) {
        return ChapterVO.builder()
                .id(id)
                .courseId("course-1")
                .title("test-chapter")
                .description("test-chapter-description")
                .duration(1800)
                .orderIndex(1)
                .isOptional(false)
                .chapterStatus("published")
                .content(Collections.emptyList())
                .build();
    }

    // ── Course entity ──────────────────────────────────────────────

    public static Course createCourse(String id) {
        return Course.builder()
                .id(id)
                .title("test-course")
                .description("test-description")
                .difficulty("easy")
                .estimatedDuration(60)
                .isPublished(true)
                .creatorId("admin1")
                .enrollmentCount(0)
                .averageRating(BigDecimal.ZERO)
                .build();
    }

    // ── PageResponse ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static PageResponse<Object> createPageResponse(List<?> data, long total, long current, long size) {
        return PageResponse.<Object>builder()
                .total(total)
                .size(size)
                .current(current)
                .list((List<Object>) data)
                .build();
    }

    public static PageResponse<Object> createEmptyPageResponse(long current, long size) {
        return PageResponse.<Object>builder()
                .total(0L)
                .size(size)
                .current(current)
                .list(Collections.emptyList())
                .build();
    }

    // ── JSON helper ────────────────────────────────────────────────

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    // ── Internal helpers ───────────────────────────────────────────

    private static ContentBlockDto createContentBlockDto() {
        return ContentBlockDto.builder()
                .blockType(BlockType.Paragraph)
                .textContent("test-content-block")
                .build();
    }
}
