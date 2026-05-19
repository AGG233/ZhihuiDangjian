package com.rauio.smartdangjian.server.content.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.content.spec.ArticleStatus;

class ArticleRequestTest {

    @Test
    @DisplayName("builder 构造 ArticleRequest 所有字段值正确")
    void builderCreatesArticleRequestCorrectly() {
        ArticleRequest request = ArticleRequest.builder()
                .id("art-001")
                .authorId("user-001")
                .title("测试文章")
                .summary("测试摘要")
                .status(ArticleStatus.Published)
                .build();

        assertThat(request.getId()).isEqualTo("art-001");
        assertThat(request.getAuthorId()).isEqualTo("user-001");
        assertThat(request.getTitle()).isEqualTo("测试文章");
        assertThat(request.getSummary()).isEqualTo("测试摘要");
        assertThat(request.getStatus()).isEqualTo(ArticleStatus.Published);
    }

    @Test
    @DisplayName("builder 构造 ArticleRequest 状态为 Draft")
    void builderCreatesArticleRequestWithDraftStatus() {
        ArticleRequest request =
                ArticleRequest.builder().title("草稿文章").status(ArticleStatus.Draft).build();

        assertThat(request.getStatus()).isEqualTo(ArticleStatus.Draft);
    }

    @Test
    @DisplayName("builder 构造 ArticleRequest id 可为 null（新增场景）")
    void builderCreatesArticleRequestWithNullId() {
        ArticleRequest request = ArticleRequest.builder()
                .authorId("user-001")
                .title("新增文章")
                .summary("新增摘要")
                .status(ArticleStatus.Draft)
                .build();

        assertThat(request.getId()).isNull();
        assertThat(request.getTitle()).isEqualTo("新增文章");
    }

    @Test
    @DisplayName("builder 构造 ArticleRequest summary 可为 null")
    void builderCreatesArticleRequestWithNullSummary() {
        ArticleRequest request = ArticleRequest.builder()
                .title("无摘要文章")
                .status(ArticleStatus.Published)
                .build();

        assertThat(request.getSummary()).isNull();
    }
}
