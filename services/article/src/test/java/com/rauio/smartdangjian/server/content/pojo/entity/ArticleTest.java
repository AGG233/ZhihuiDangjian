package com.rauio.smartdangjian.server.content.pojo.entity;

import com.rauio.smartdangjian.server.content.spec.ArticleStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleTest {

    @Test
    @DisplayName("builder 构造 Article 所有字段值正确")
    void builderCreatesArticleCorrectly() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 1, 12, 0);
        Article article = Article.builder()
                .id("art-001")
                .authorId("user-001")
                .title("测试文章标题")
                .summary("测试文章摘要")
                .status(ArticleStatus.Published)
                .publishedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(article.getId()).isEqualTo("art-001");
        assertThat(article.getAuthorId()).isEqualTo("user-001");
        assertThat(article.getTitle()).isEqualTo("测试文章标题");
        assertThat(article.getSummary()).isEqualTo("测试文章摘要");
        assertThat(article.getStatus()).isEqualTo(ArticleStatus.Published);
        assertThat(article.getPublishedAt()).isEqualTo(now);
        assertThat(article.getCreatedAt()).isEqualTo(now);
        assertThat(article.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder 构造 Article Status 为 Draft")
    void builderCreatesArticleWithDraftStatus() {
        Article article = Article.builder()
                .id("art-002")
                .status(ArticleStatus.Draft)
                .build();

        assertThat(article.getStatus()).isEqualTo(ArticleStatus.Draft);
    }

    @Test
    @DisplayName("builder 构造 Article Status 为 Deleted(archived)")
    void builderCreatesArticleWithArchivedStatus() {
        Article article = Article.builder()
                .id("art-003")
                .status(ArticleStatus.Deleted)
                .build();

        assertThat(article.getStatus()).isEqualTo(ArticleStatus.Deleted);
    }

    @Test
    @DisplayName("setter 修改 title 后 getter 返回新值")
    void setterAndGetterWorkForTitle() {
        Article article = Article.builder().id("art-001").title("旧标题").build();

        article.setTitle("新标题");

        assertThat(article.getTitle()).isEqualTo("新标题");
    }

    @Test
    @DisplayName("setter 修改 status 后 getter 返回新值")
    void setterAndGetterWorkForStatus() {
        Article article = Article.builder().id("art-001").status(ArticleStatus.Draft).build();

        article.setStatus(ArticleStatus.Published);

        assertThat(article.getStatus()).isEqualTo(ArticleStatus.Published);
    }

    @Test
    @DisplayName("toString 包含所有主要字段")
    void toStringContainsKeyFields() {
        Article article = Article.builder()
                .id("art-001")
                .title("文章标题")
                .status(ArticleStatus.Published)
                .build();

        String str = article.toString();

        assertThat(str).contains("art-001", "文章标题", "Published");
    }

    @Test
    @DisplayName("两个相同字段的 Article equals 和 hashCode 行为符合 @Data 预期")
    void equalsAndHashCodeBehavior() {
        Article a1 = Article.builder().id("art-001").title("标题").status(ArticleStatus.Draft).build();
        Article a2 = Article.builder().id("art-001").title("标题").status(ArticleStatus.Draft).build();

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    @DisplayName("两个不同 id 的 Article 不相等")
    void articlesWithDifferentIdsAreNotEqual() {
        Article a1 = Article.builder().id("art-001").title("标题").build();
        Article a2 = Article.builder().id("art-002").title("标题").build();

        assertThat(a1).isNotEqualTo(a2);
    }

    @Test
    @DisplayName("publishedAt 为 null 时 builder 正常工作")
    void builderWithNullPublishedAt() {
        Article article = Article.builder()
                .id("art-001")
                .title("草稿")
                .status(ArticleStatus.Draft)
                .publishedAt(null)
                .build();

        assertThat(article.getPublishedAt()).isNull();
    }
}
