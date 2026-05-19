package com.rauio.smartdangjian.server.content.pojo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseDtoTest {

    @Test
    @DisplayName("builder 构造 CourseDto 所有字段值正确")
    void builderCreatesCourseDtoCorrectly() {
        CourseDto dto = CourseDto.builder()
                .title("课程标题")
                .description("课程描述")
                .coverImageId("img-001")
                .categoryId("cat-001")
                .difficulty("beginner")
                .estimatedDuration(60)
                .isPublished(true)
                .build();

        assertThat(dto.getTitle()).isEqualTo("课程标题");
        assertThat(dto.getDescription()).isEqualTo("课程描述");
        assertThat(dto.getCoverImageId()).isEqualTo("img-001");
        assertThat(dto.getCategoryId()).isEqualTo("cat-001");
        assertThat(dto.getDifficulty()).isEqualTo("beginner");
        assertThat(dto.getEstimatedDuration()).isEqualTo(60);
        assertThat(dto.getIsPublished()).isTrue();
    }

    @Test
    @DisplayName("builder 构造未发布课程 DTO")
    void builderCreatesUnpublishedCourseDto() {
        CourseDto dto = CourseDto.builder()
                .title("草稿课程")
                .categoryId("cat-001")
                .isPublished(false)
                .build();

        assertThat(dto.getIsPublished()).isFalse();
    }

    @Test
    @DisplayName("description 和 coverImageId 可为 null")
    void optionalFieldsCanBeNull() {
        CourseDto dto = CourseDto.builder().title("最简课程").categoryId("cat-001").build();

        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getCoverImageId()).isNull();
        assertThat(dto.getDifficulty()).isNull();
    }

    @Test
    @DisplayName("setter 修改 title 后 getter 返回新值")
    void setterAndGetterWorkForTitle() {
        CourseDto dto = CourseDto.builder().title("旧标题").categoryId("cat-001").build();

        dto.setTitle("新标题");

        assertThat(dto.getTitle()).isEqualTo("新标题");
    }
}
