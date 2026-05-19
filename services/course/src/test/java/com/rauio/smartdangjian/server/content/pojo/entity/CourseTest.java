package com.rauio.smartdangjian.server.content.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseTest {

    @Test
    @DisplayName("builder 构造 Course 所有字段值正确")
    void builderCreatesCourseCorrectly() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 1, 12, 0);
        Course course = Course.builder()
                .id("course-001")
                .title("课程标题")
                .description("课程描述")
                .coverImageId("img-001")
                .difficulty("beginner")
                .estimatedDuration(60)
                .creatorId("user-001")
                .enrollmentCount(100)
                .averageRating(new BigDecimal("4.5"))
                .isPublished(true)
                .publishedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(course.getId()).isEqualTo("course-001");
        assertThat(course.getTitle()).isEqualTo("课程标题");
        assertThat(course.getDescription()).isEqualTo("课程描述");
        assertThat(course.getCoverImageId()).isEqualTo("img-001");
        assertThat(course.getDifficulty()).isEqualTo("beginner");
        assertThat(course.getEstimatedDuration()).isEqualTo(60);
        assertThat(course.getCreatorId()).isEqualTo("user-001");
        assertThat(course.getEnrollmentCount()).isEqualTo(100);
        assertThat(course.getAverageRating()).isEqualTo(new BigDecimal("4.5"));
        assertThat(course.getIsPublished()).isTrue();
        assertThat(course.getPublishedAt()).isEqualTo(now);
        assertThat(course.getCreatedAt()).isEqualTo(now);
        assertThat(course.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder 构造未发布课程")
    void builderCreatesUnpublishedCourse() {
        Course course = Course.builder()
                .id("course-002")
                .title("草稿课程")
                .isPublished(false)
                .enrollmentCount(0)
                .averageRating(BigDecimal.ZERO)
                .build();

        assertThat(course.getIsPublished()).isFalse();
        assertThat(course.getEnrollmentCount()).isEqualTo(0);
        assertThat(course.getAverageRating()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("setter 修改 title 后 getter 返回新值")
    void setterAndGetterWorkForTitle() {
        Course course = Course.builder().id("course-001").title("旧标题").build();

        course.setTitle("新标题");

        assertThat(course.getTitle()).isEqualTo("新标题");
    }

    @Test
    @DisplayName("setter 修改 difficulty 后 getter 返回新值")
    void setterAndGetterWorkForDifficulty() {
        Course course = Course.builder().id("course-001").difficulty("beginner").build();

        course.setDifficulty("advanced");

        assertThat(course.getDifficulty()).isEqualTo("advanced");
    }

    @Test
    @DisplayName("两个相同字段的 Course equals 和 hashCode 行为符合 @Data 预期")
    void equalsAndHashCodeBehavior() {
        Course c1 = Course.builder()
                .id("course-001")
                .title("课程")
                .difficulty("beginner")
                .build();
        Course c2 = Course.builder()
                .id("course-001")
                .title("课程")
                .difficulty("beginner")
                .build();

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    @DisplayName("两个不同 id 的 Course 不相等")
    void coursesWithDifferentIdsAreNotEqual() {
        Course c1 = Course.builder().id("course-001").title("课程").build();
        Course c2 = Course.builder().id("course-002").title("课程").build();

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    @DisplayName("isPublished 为 null 时 builder 正常工作")
    void builderWithNullIsPublished() {
        Course course =
                Course.builder().id("course-001").title("课程").isPublished(null).build();

        assertThat(course.getIsPublished()).isNull();
    }
}
