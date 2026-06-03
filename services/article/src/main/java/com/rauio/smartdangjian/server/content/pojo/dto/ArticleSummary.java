package com.rauio.smartdangjian.server.content.pojo.dto;

import com.rauio.smartdangjian.server.content.pojo.entity.Article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果中的文章摘要 —— 不暴露文章实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSummary {
    private Long id;
    private String title;
    private String summary;

    public static ArticleSummary from(Article article) {
        if (article == null) return null;
        return ArticleSummary.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .build();
    }
}
