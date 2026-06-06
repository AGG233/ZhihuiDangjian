package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.api.ArticleQueryFacade;
import com.rauio.smartdangjian.server.content.api.ContentQueryFacade;
import com.rauio.smartdangjian.server.content.api.dto.ArticleSummary;

@ExtendWith(MockitoExtension.class)
class ArticleDetailToolTest {

    @Mock
    private ArticleQueryFacade articleQueryFacade;

    @Mock
    private ContentQueryFacade contentQueryFacade;

    @InjectMocks
    private ArticleDetailTool articleDetailTool;

    @Test
    @DisplayName("searchArticles 根据关键词搜索文章并返回映射列表")
    void searchArticles() {
        ArticleSummary article = ArticleSummary.builder()
                .id(1L)
                .title("党建理论学习")
                .summary("深入理解党的理论")
                .build();

        when(articleQueryFacade.searchByKeyword("党建")).thenReturn(List.of(article));

        List<Map<String, Object>> result = articleDetailTool.searchArticles("党建");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("id", 1L);
        assertThat(result.get(0)).containsEntry("title", "党建理论学习");
        assertThat(result.get(0)).containsEntry("summary", "深入理解党的理论");
        verify(articleQueryFacade, times(1)).searchByKeyword("党建");
    }

    @Test
    @DisplayName("searchArticles 无匹配结果时返回空列表")
    void searchArticlesNoResults() {
        when(articleQueryFacade.searchByKeyword("不存在")).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = articleDetailTool.searchArticles("不存在");

        assertThat(result).isEmpty();
        verify(articleQueryFacade, times(1)).searchByKeyword("不存在");
    }

    @Test
    @DisplayName("getArticleDetail 返回文章详情和内容块")
    void getArticleDetail() {
        ArticleSummary article = ArticleSummary.builder()
                .id(1L)
                .title("党建理论学习")
                .summary("深入理解党的理论")
                .build();

        when(articleQueryFacade.getById(1L)).thenReturn(article);
        when(contentQueryFacade.getByArticleId(1L)).thenReturn(List.of());

        Map<String, Object> result = articleDetailTool.getArticleDetail("1");

        assertThat(result).containsEntry("id", 1L);
        assertThat(result).containsEntry("title", "党建理论学习");
        assertThat(result).containsKey("contentBlocks");
        verify(articleQueryFacade, times(1)).getById(1L);
        verify(contentQueryFacade, times(1)).getByArticleId(1L);
    }

    @Test
    @DisplayName("getArticleDetail 文章不存在时抛出 BusinessException")
    void getArticleDetailNotFound() {
        when(articleQueryFacade.getById(999L)).thenReturn(null);

        assertThatThrownBy(() -> articleDetailTool.getArticleDetail("999"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文章不存在");
        verify(articleQueryFacade, times(1)).getById(999L);
    }
}
