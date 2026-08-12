package com.rauio.smartdangjian.server.content.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.content.spec.ArticleStatus;

class ArticleResponseTest {

    @Test
    @DisplayName("builder 构造 ArticleResponse 所有字段值正确")
    void builderCreatesArticleResponseCorrectly() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);
        ArticleResponse response = ArticleResponse.builder()
                .id(1L)
                .authorId(1L)
                .title("测试文章")
                .summary("测试摘要")
                .status(ArticleStatus.Published)
                .publishedAt(now)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAuthorId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("测试文章");
        assertThat(response.getSummary()).isEqualTo("测试摘要");
        assertThat(response.getStatus()).isEqualTo(ArticleStatus.Published);
        assertThat(response.getPublishedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder 构造 ArticleResponse 发布时间可为 null")
    void builderCreatesArticleResponseWithNullPublishedAt() {
        ArticleResponse response = ArticleResponse.builder()
                .id(1L)
                .authorId(1L)
                .title("未发布文章")
                .status(ArticleStatus.Draft)
                .build();

        assertThat(response.getPublishedAt()).isNull();
    }
}
