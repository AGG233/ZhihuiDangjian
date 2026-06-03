package com.rauio.smartdangjian.server.content.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.content.api.dto.ArticleSummary;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.spec.ArticleStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleQueryFacadeImpl")
class ArticleQueryFacadeImplTest {

    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private ArticleQueryFacadeImpl facade;

    private Article publishedArticle() {
        return Article.builder()
                .id(1L)
                .title("党建文章")
                .summary("摘要")
                .authorId(100L)
                .status(ArticleStatus.Published)
                .publishedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    private Article draftArticle() {
        return Article.builder()
                .id(2L)
                .title("草稿文章")
                .summary("草稿")
                .authorId(100L)
                .status(ArticleStatus.Draft)
                .build();
    }

    @Nested
    @DisplayName("getById 方法")
    class GetById {

        @Test
        @DisplayName("已发布文章返回 ArticleSummary")
        void publishedReturnsSummary() {
            when(articleMapper.selectById(1L)).thenReturn(publishedArticle());

            ArticleSummary result = facade.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("党建文章");
            assertThat(result.getSummary()).isEqualTo("摘要");
            assertThat(result.getAuthorId()).isEqualTo(100L);
            assertThat(result.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("文章不存在返回 null")
        void notFoundReturnsNull() {
            when(articleMapper.selectById(99L)).thenReturn(null);

            ArticleSummary result = facade.getById(99L);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("草稿文章返回 null")
        void draftReturnsNull() {
            when(articleMapper.selectById(2L)).thenReturn(draftArticle());

            ArticleSummary result = facade.getById(2L);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("参数为 null 返回 null")
        void nullIdReturnsNull() {
            ArticleSummary result = facade.getById(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("searchByKeyword 方法")
    class SearchByKeyword {

        @Test
        @DisplayName("关键词匹配返回已发布文章列表")
        void keywordReturnsPublishedArticles() {
            when(articleMapper.selectList(any())).thenReturn(List.of(publishedArticle()));

            List<ArticleSummary> results = facade.searchByKeyword("党建");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("党建文章");
        }

        @Test
        @DisplayName("无匹配结果返回空列表")
        void noMatchReturnsEmpty() {
            when(articleMapper.selectList(any())).thenReturn(List.of());

            List<ArticleSummary> results = facade.searchByKeyword("不存在的文章");

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("空关键词查询所有已发布文章")
        void blankKeywordReturnsAllPublished() {
            when(articleMapper.selectList(any())).thenReturn(List.of(publishedArticle()));

            List<ArticleSummary> results = facade.searchByKeyword("");

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("关键词前后空格不影响查询结果")
        void keywordWithTrim() {
            when(articleMapper.selectList(any())).thenReturn(List.of(publishedArticle()));

            List<ArticleSummary> results = facade.searchByKeyword("  党建  ");

            assertThat(results).hasSize(1);
        }
    }
}
