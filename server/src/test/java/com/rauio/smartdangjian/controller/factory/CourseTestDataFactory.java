package com.rauio.smartdangjian.controller.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.content.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.dto.CourseDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.server.content.pojo.vo.PageVO;
import com.rauio.smartdangjian.server.content.spec.BlockType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Static factory for course/chapter test data — produces CourseDto, CourseVO,
 * ChapterDto, ChapterVO, PageVO, Course, and JSON helpers.
 * All IDs are deterministic strings so jsonPath assertions are predictable.
 */
public final class CourseTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private CourseTestDataFactory() {
    }

    // ── CourseDto ──────────────────────────────────────────────────

    public static CourseDto createCourseDto() {
        return CourseDto.builder()
                .title("test-course")
                .description("test-description")
                .categoryId("cat-1")
                .difficulty("easy")
                .estimatedDuration(60)
                .isPublished(true)
                .build();
    }

    // ── CourseVO ───────────────────────────────────────────────────

    public static CourseVO createCourseVO(String id) {
        return CourseVO.builder()
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

    // ── PageVO ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static PageVO<Object> createPageVO(List<?> data, long total, long current, long size) {
        return PageVO.<Object>builder()
                .total(total)
                .size(size)
                .current(current)
                .list((List<Object>) data)
                .build();
    }

    public static PageVO<Object> createEmptyPageVO(long current, long size) {
        return PageVO.<Object>builder()
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
