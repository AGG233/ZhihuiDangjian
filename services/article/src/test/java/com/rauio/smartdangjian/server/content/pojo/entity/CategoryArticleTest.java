package com.rauio.smartdangjian.server.content.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryArticleTest {

    @Test
    @DisplayName("getter/setter 设置 categoryId 和 articleId 后正常返回")
    void gettersAndSettersWorkCorrectly() {
        CategoryArticle ca = new CategoryArticle();
        ca.setCategoryId(1L);
        ca.setArticleId(1L);

        assertThat(ca.getCategoryId()).isEqualTo(1L);
        assertThat(ca.getArticleId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("两个相同字段的 CategoryArticle equals 和 hashCode 行为符合 @Data 预期")
    void equalsAndHashCodeBehavior() {
        CategoryArticle ca1 = new CategoryArticle();
        ca1.setCategoryId(1L);
        ca1.setArticleId(1L);

        CategoryArticle ca2 = new CategoryArticle();
        ca2.setCategoryId(1L);
        ca2.setArticleId(1L);

        assertThat(ca1).isEqualTo(ca2);
        assertThat(ca1.hashCode()).isEqualTo(ca2.hashCode());
    }

    @Test
    @DisplayName("toString 包含 categoryId 和 articleId")
    void toStringContainsFields() {
        CategoryArticle ca = new CategoryArticle();
        ca.setCategoryId(1L);
        ca.setArticleId(1L);

        String str = ca.toString();

        assertThat(str).contains("cat-001", "art-001");
    }
}
