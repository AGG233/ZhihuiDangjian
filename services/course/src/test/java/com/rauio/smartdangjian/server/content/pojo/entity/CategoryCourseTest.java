package com.rauio.smartdangjian.server.content.pojo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryCourseTest {

    @Test
    @DisplayName("builder 构造 CategoryCourse 字段值正确")
    void builderCreatesCategoryCourseCorrectly() {
        CategoryCourse cc = CategoryCourse.builder()
                .categoryId("cat-001")
                .courseId("course-001")
                .build();

        assertThat(cc.getCategoryId()).isEqualTo("cat-001");
        assertThat(cc.getCourseId()).isEqualTo("course-001");
    }

    @Test
    @DisplayName("setter 修改 categoryId 后 getter 返回新值")
    void setterAndGetterWorkForCategoryId() {
        CategoryCourse cc = CategoryCourse.builder()
                .categoryId("cat-001")
                .courseId("course-001")
                .build();

        cc.setCategoryId("cat-002");

        assertThat(cc.getCategoryId()).isEqualTo("cat-002");
    }

    @Test
    @DisplayName("两个相同字段的 CategoryCourse equals 和 hashCode 行为符合 @Data 预期")
    void equalsAndHashCodeBehavior() {
        CategoryCourse cc1 = CategoryCourse.builder().categoryId("cat-001").courseId("course-001").build();
        CategoryCourse cc2 = CategoryCourse.builder().categoryId("cat-001").courseId("course-001").build();

        assertThat(cc1).isEqualTo(cc2);
        assertThat(cc1.hashCode()).isEqualTo(cc2.hashCode());
    }
}
