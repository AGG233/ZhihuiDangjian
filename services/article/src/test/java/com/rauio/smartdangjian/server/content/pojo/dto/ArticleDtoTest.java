package com.rauio.smartdangjian.server.content.pojo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.content.spec.ArticleStatus;

class ArticleDtoTest {

    @Test
    @DisplayName("builder 构造 ArticleDto 所有字段值正确")
    void builderCreatesArticleDtoCorrectly() {
        ArticleDto dto = ArticleDto.builder()
                .id("art-001")
                .authorId("user-001")
                .title("文章标题")
                .summary("文章摘要")
                .status(ArticleStatus.Published)
                .build();

        assertThat(dto.getId()).isEqualTo("art-001");
        assertThat(dto.getAuthorId()).isEqualTo("user-001");
        assertThat(dto.getTitle()).isEqualTo("文章标题");
        assertThat(dto.getSummary()).isEqualTo("文章摘要");
        assertThat(dto.getStatus()).isEqualTo(ArticleStatus.Published);
    }

    @Test
    @DisplayName("builder 构造 ArticleDto 状态为 Draft")
    void builderCreatesArticleDtoWithDraftStatus() {
        ArticleDto dto =
                ArticleDto.builder().title("草稿文章").status(ArticleStatus.Draft).build();

        assertThat(dto.getStatus()).isEqualTo(ArticleStatus.Draft);
    }

    @Test
    @DisplayName("id 字段默认留空为 null")
    void idFieldDefaultsToNull() {
        ArticleDto dto = ArticleDto.builder()
                .title("文章")
                .authorId("user-001")
                .status(ArticleStatus.Draft)
                .build();

        assertThat(dto.getId()).isNull();
    }

    @Test
    @DisplayName("setter 修改 summary 后 getter 返回新值")
    void setterAndGetterWorkForSummary() {
        ArticleDto dto = ArticleDto.builder().summary("旧摘要").build();

        dto.setSummary("新摘要");

        assertThat(dto.getSummary()).isEqualTo("新摘要");
    }
}
