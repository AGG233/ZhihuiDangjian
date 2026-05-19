package com.rauio.smartdangjian.server.content.pojo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryArticleTest {

    @Test
    @DisplayName("getter/setter 设置 categoryId 和 articleId 后正常返回")
    void gettersAndSettersWorkCorrectly() {
        CategoryArticle ca = new CategoryArticle();
        ca.setCategoryId("cat-001");
        ca.setArticleId("art-001");

        assertThat(ca.getCategoryId()).isEqualTo("cat-001");
        assertThat(ca.getArticleId()).isEqualTo("art-001");
    }

    @Test
    @DisplayName("两个相同字段的 CategoryArticle equals 和 hashCode 行为符合 @Data 预期")
    void equalsAndHashCodeBehavior() {
        CategoryArticle ca1 = new CategoryArticle();
        ca1.setCategoryId("cat-001");
        ca1.setArticleId("art-001");

        CategoryArticle ca2 = new CategoryArticle();
        ca2.setCategoryId("cat-001");
        ca2.setArticleId("art-001");

        assertThat(ca1).isEqualTo(ca2);
        assertThat(ca1.hashCode()).isEqualTo(ca2.hashCode());
    }

    @Test
    @DisplayName("toString 包含 categoryId 和 articleId")
    void toStringContainsFields() {
        CategoryArticle ca = new CategoryArticle();
        ca.setCategoryId("cat-001");
        ca.setArticleId("art-001");

        String str = ca.toString();

        assertThat(str).contains("cat-001", "art-001");
    }
}
