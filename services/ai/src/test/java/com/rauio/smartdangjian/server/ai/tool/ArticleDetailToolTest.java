package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.service.ContentBlockService;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;

@ExtendWith(MockitoExtension.class)
class ArticleDetailToolTest {

    @Mock
    private ArticleService articleService;

    @Mock
    private ContentBlockService contentBlockService;

    @InjectMocks
    private ArticleDetailTool articleDetailTool;

    @Test
    @DisplayName("searchArticles 根据关键词搜索文章并返回映射列表")
    void searchArticles() {
        Article article = Article.builder()
                .id("article-1")
                .title("党建理论学习")
                .summary("深入理解党的理论")
                .build();

        when(articleService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(article));

        List<Map<String, Object>> result = articleDetailTool.searchArticles("党建");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("id", "article-1");
        assertThat(result.get(0)).containsEntry("title", "党建理论学习");
        assertThat(result.get(0)).containsEntry("summary", "深入理解党的理论");
    }

    @Test
    @DisplayName("searchArticles 无匹配结果时返回空列表")
    void searchArticlesNoResults() {
        when(articleService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Map<String, Object>> result = articleDetailTool.searchArticles("不存在");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getArticleDetail 返回文章详情和内容块")
    void getArticleDetail() {
        Article article = Article.builder()
                .id("article-1")
                .title("党建理论学习")
                .summary("深入理解党的理论")
                .build();

        when(articleService.getById("article-1")).thenReturn(article);
        when(contentBlockService.getByParentId("article-1")).thenReturn(List.of());

        Map<String, Object> result = articleDetailTool.getArticleDetail("article-1");

        assertThat(result).containsEntry("id", "article-1");
        assertThat(result).containsEntry("title", "党建理论学习");
        assertThat(result).containsKey("contentBlocks");
    }

    @Test
    @DisplayName("getArticleDetail 文章不存在时抛出 BusinessException")
    void getArticleDetailNotFound() {
        when(articleService.getById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> articleDetailTool.getArticleDetail("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文章不存在");
    }
}
