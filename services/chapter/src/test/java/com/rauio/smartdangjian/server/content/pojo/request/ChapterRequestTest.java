package com.rauio.smartdangjian.server.content.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChapterRequestTest {

    @Test
    @DisplayName("builder 构造 ChapterRequest 所有字段值正确")
    void builderCreatesChapterRequestCorrectly() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("course-001")
                .title("章节标题")
                .description("章节描述")
                .duration(1800)
                .orderIndex(1)
                .isOptional(false)
                .chapterStatus("published")
                .content(List.of())
                .build();

        assertThat(dto.getCourseId()).isEqualTo("course-001");
        assertThat(dto.getTitle()).isEqualTo("章节标题");
        assertThat(dto.getDescription()).isEqualTo("章节描述");
        assertThat(dto.getDuration()).isEqualTo(1800);
        assertThat(dto.getOrderIndex()).isEqualTo(1);
        assertThat(dto.getIsOptional()).isFalse();
        assertThat(dto.getChapterStatus()).isEqualTo("published");
        assertThat(dto.getContent()).isEmpty();
    }

    @Test
    @DisplayName("builder 默认 duration 为 -1")
    void defaultDurationIsMinusOne() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("course-001")
                .title("标题")
                .description("描述")
                .orderIndex(1)
                .content(List.of())
                .build();

        assertThat(dto.getDuration()).isEqualTo(-1);
    }

    @Test
    @DisplayName("builder 默认 isOptional 为 false")
    void defaultIsOptionalIsFalse() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("course-001")
                .title("标题")
                .description("描述")
                .orderIndex(1)
                .content(List.of())
                .build();

        assertThat(dto.getIsOptional()).isFalse();
    }

    @Test
    @DisplayName("builder 默认 chapterStatus 为 draft")
    void defaultChapterStatusIsDraft() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("course-001")
                .title("标题")
                .description("描述")
                .orderIndex(1)
                .content(List.of())
                .build();

        assertThat(dto.getChapterStatus()).isEqualTo("draft");
    }

    @Test
    @DisplayName("setter 修改 title 后 getter 返回新值")
    void setterAndGetterWorkForTitle() {
        ChapterRequest dto = ChapterRequest.builder()
                .courseId("course-001")
                .title("旧标题")
                .description("描述")
                .orderIndex(1)
                .content(List.of())
                .build();

        dto.setTitle("新标题");

        assertThat(dto.getTitle()).isEqualTo("新标题");
    }
}
