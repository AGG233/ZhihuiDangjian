package com.rauio.smartdangjian.server.content.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChapterTest {

    @Test
    @DisplayName("builder 构造 Chapter 所有字段值正确")
    void builderCreatesChapterCorrectly() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 1, 12, 0);
        Chapter chapter = Chapter.builder()
                .id("ch-001")
                .courseId("course-001")
                .title("第一章")
                .description("章节描述")
                .duration(1800)
                .orderIndex(1)
                .isOptional(false)
                .chapterStatus("published")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(chapter.getId()).isEqualTo("ch-001");
        assertThat(chapter.getCourseId()).isEqualTo("course-001");
        assertThat(chapter.getTitle()).isEqualTo("第一章");
        assertThat(chapter.getDescription()).isEqualTo("章节描述");
        assertThat(chapter.getDuration()).isEqualTo(1800);
        assertThat(chapter.getOrderIndex()).isEqualTo(1);
        assertThat(chapter.getIsOptional()).isFalse();
        assertThat(chapter.getChapterStatus()).isEqualTo("published");
        assertThat(chapter.getCreatedAt()).isEqualTo(now);
        assertThat(chapter.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder 构造 Chapter 状态为 draft")
    void builderCreatesChapterWithDraftStatus() {
        Chapter chapter = Chapter.builder()
                .id("ch-002")
                .courseId("course-001")
                .title("草稿章节")
                .chapterStatus("draft")
                .isOptional(true)
                .build();

        assertThat(chapter.getChapterStatus()).isEqualTo("draft");
        assertThat(chapter.getIsOptional()).isTrue();
    }

    @Test
    @DisplayName("setter 修改 title 后 getter 返回新值")
    void setterAndGetterWorkForTitle() {
        Chapter chapter = Chapter.builder().id("ch-001").title("旧标题").build();

        chapter.setTitle("新标题");

        assertThat(chapter.getTitle()).isEqualTo("新标题");
    }

    @Test
    @DisplayName("setter 修改 orderIndex 后 getter 返回新值")
    void setterAndGetterWorkForOrderIndex() {
        Chapter chapter = Chapter.builder().id("ch-001").orderIndex(1).build();

        chapter.setOrderIndex(9);

        assertThat(chapter.getOrderIndex()).isEqualTo(9);
    }

    @Test
    @DisplayName("两个相同字段的 Chapter equals 和 hashCode 行为符合 @Data 预期")
    void equalsAndHashCodeBehavior() {
        Chapter c1 = Chapter.builder()
                .id("ch-001")
                .courseId("course-001")
                .title("章节")
                .build();
        Chapter c2 = Chapter.builder()
                .id("ch-001")
                .courseId("course-001")
                .title("章节")
                .build();

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    @DisplayName("两个不同 id 的 Chapter 不相等")
    void chaptersWithDifferentIdsAreNotEqual() {
        Chapter c1 = Chapter.builder().id("ch-001").title("章节").build();
        Chapter c2 = Chapter.builder().id("ch-002").title("章节").build();

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    @DisplayName("toString 包含主要字段")
    void toStringContainsKeyFields() {
        Chapter chapter = Chapter.builder()
                .id("ch-001")
                .title("第一章")
                .chapterStatus("published")
                .build();

        String str = chapter.toString();

        assertThat(str).contains("ch-001", "第一章", "published");
    }
}
