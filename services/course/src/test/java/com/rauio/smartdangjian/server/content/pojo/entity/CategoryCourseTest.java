package com.rauio.smartdangjian.server.content.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryCourseTest {

    @Test
    @DisplayName("builder 构造 CategoryCourse 字段值正确")
    void builderCreatesCategoryCourseCorrectly() {
        CategoryCourse cc = CategoryCourse.builder().categoryId(1L).courseId(1L).build();

        assertThat(cc.getCategoryId()).isEqualTo(1L);
        assertThat(cc.getCourseId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("setter 修改 categoryId 后 getter 返回新值")
    void setterAndGetterWorkForCategoryId() {
        CategoryCourse cc = CategoryCourse.builder().categoryId(1L).courseId(1L).build();

        cc.setCategoryId(1L);

        assertThat(cc.getCategoryId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("两个相同字段的 CategoryCourse equals 和 hashCode 行为符合 @Data 预期")
    void equalsAndHashCodeBehavior() {
        CategoryCourse cc1 =
                CategoryCourse.builder().categoryId(1L).courseId(1L).build();
        CategoryCourse cc2 =
                CategoryCourse.builder().categoryId(1L).courseId(1L).build();

        assertThat(cc1).isEqualTo(cc2);
        assertThat(cc1.hashCode()).isEqualTo(cc2.hashCode());
    }
}
