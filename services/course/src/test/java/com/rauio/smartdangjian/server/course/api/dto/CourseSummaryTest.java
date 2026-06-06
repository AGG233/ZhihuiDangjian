package com.rauio.smartdangjian.server.course.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.course.pojo.entity.Course;

class CourseSummaryTest {

    @Test
    @DisplayName("from(Course) 复制所有指定字段值正确")
    void fromCourseCopiesAllFieldsCorrectly() {
        Course course = Course.builder()
                .id(1L)
                .title("课程标题")
                .description("课程描述")
                .difficulty("beginner")
                .coverImageId(100L)
                .estimatedDuration(60)
                .enrollmentCount(50)
                .build();

        CourseSummary summary = CourseSummary.from(course);

        assertThat(summary.getId()).isEqualTo(1L);
        assertThat(summary.getTitle()).isEqualTo("课程标题");
        assertThat(summary.getDescription()).isEqualTo("课程描述");
        assertThat(summary.getDifficulty()).isEqualTo("beginner");
        assertThat(summary.getCoverImageId()).isEqualTo(100L);
        assertThat(summary.getEnrollmentCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("from(null) 返回 null")
    void fromNullReturnsNull() {
        assertThat(CourseSummary.from(null)).isNull();
    }

    @Test
    @DisplayName("from(Course) 不复制 estimatedDuration 等非摘要字段")
    void fromCourseDoesNotCopyNonSummaryFields() {
        Course course =
                Course.builder().id(1L).title("课程标题").estimatedDuration(120).build();

        CourseSummary summary = CourseSummary.from(course);

        // estimatedDuration 不在 CourseSummary 字段集中，仅确认标题等摘要字段存在
        assertThat(summary.getId()).isEqualTo(1L);
        assertThat(summary.getTitle()).isEqualTo("课程标题");
        assertThat(summary.getDescription()).isNull();
        assertThat(summary.getDifficulty()).isNull();
        assertThat(summary.getCoverImageId()).isNull();
        assertThat(summary.getEnrollmentCount()).isNull();
    }

    @Test
    @DisplayName("from(Course) 处理部分 null 字段时不抛异常")
    void fromCourseWithNullFieldsDoesNotThrow() {
        Course course = Course.builder().id(1L).title("标题").build();

        CourseSummary summary = CourseSummary.from(course);

        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(1L);
        assertThat(summary.getTitle()).isEqualTo("标题");
        assertThat(summary.getDescription()).isNull();
    }
}
