package com.rauio.smartdangjian.server.content.pojo.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CourseVOTest {

    @Test
    @DisplayName("builder 构造 CourseVO 所有字段值正确")
    void builderCreatesCourseVOCorrectly() {
        LocalDateTime publishedAt = LocalDateTime.of(2025, 6, 1, 12, 0);
        CourseVO vo = CourseVO.builder()
                .id("course-001")
                .title("课程标题")
                .description("课程描述")
                .categoryId("cat-001")
                .difficulty("beginner")
                .coverImageId("img-001")
                .estimatedDuration(60)
                .enrollmentCount(100)
                .averageRating(new BigDecimal("4.5"))
                .publishedAt(publishedAt)
                .creatorId("user-001")
                .build();

        assertThat(vo.getId()).isEqualTo("course-001");
        assertThat(vo.getTitle()).isEqualTo("课程标题");
        assertThat(vo.getDescription()).isEqualTo("课程描述");
        assertThat(vo.getCategoryId()).isEqualTo("cat-001");
        assertThat(vo.getDifficulty()).isEqualTo("beginner");
        assertThat(vo.getCoverImageId()).isEqualTo("img-001");
        assertThat(vo.getEstimatedDuration()).isEqualTo(60);
        assertThat(vo.getEnrollmentCount()).isEqualTo(100);
        assertThat(vo.getAverageRating()).isEqualTo(new BigDecimal("4.5"));
        assertThat(vo.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(vo.getCreatorId()).isEqualTo("user-001");
    }

    @Test
    @DisplayName("categoryId 为 null 时 builder 正常工作")
    void builderWithNullCategoryId() {
        CourseVO vo = CourseVO.builder()
                .id("course-001")
                .title("无分类课程")
                .categoryId(null)
                .build();

        assertThat(vo.getCategoryId()).isNull();
    }

    @Test
    @DisplayName("setter 修改 title 后 getter 返回新值")
    void setterAndGetterWorkForTitle() {
        CourseVO vo = CourseVO.builder().id("course-001").title("旧标题").build();

        vo.setTitle("新标题");

        assertThat(vo.getTitle()).isEqualTo("新标题");
    }
}
