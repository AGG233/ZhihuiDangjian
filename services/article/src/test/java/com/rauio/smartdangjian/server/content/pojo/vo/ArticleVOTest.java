package com.rauio.smartdangjian.server.content.pojo.vo;

import com.rauio.smartdangjian.server.content.spec.ArticleStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleVOTest {

    @Test
    @DisplayName("builder 构造 ArticleVO 所有字段值正确")
    void builderCreatesArticleVOCorrectly() {
        LocalDateTime publishedAt = LocalDateTime.of(2025, 6, 1, 12, 0);
        ArticleVO vo = ArticleVO.builder()
                .id("art-001")
                .authorId("user-001")
                .title("文章标题")
                .summary("文章摘要")
                .status(ArticleStatus.Published)
                .publishedAt(publishedAt)
                .build();

        assertThat(vo.getId()).isEqualTo("art-001");
        assertThat(vo.getAuthorId()).isEqualTo("user-001");
        assertThat(vo.getTitle()).isEqualTo("文章标题");
        assertThat(vo.getSummary()).isEqualTo("文章摘要");
        assertThat(vo.getStatus()).isEqualTo(ArticleStatus.Published);
        assertThat(vo.getPublishedAt()).isEqualTo(publishedAt);
    }

    @Test
    @DisplayName("publishedAt 为 null 时 builder 正常工作")
    void builderWithNullPublishedAt() {
        ArticleVO vo = ArticleVO.builder()
                .id("art-001")
                .title("草稿文章")
                .status(ArticleStatus.Draft)
                .publishedAt(null)
                .build();

        assertThat(vo.getPublishedAt()).isNull();
    }
}
