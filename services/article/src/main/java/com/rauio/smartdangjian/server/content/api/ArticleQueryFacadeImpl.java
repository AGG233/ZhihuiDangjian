package com.rauio.smartdangjian.server.content.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.server.content.api.dto.ArticleSummary;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.spec.ArticleStatus;

import lombok.RequiredArgsConstructor;

/**
 * ArticleQueryFacade 实现。通过 ArticleMapper 直接查询，按 status=PUBLISHED 过滤。
 */
@Service
@RequiredArgsConstructor
public class ArticleQueryFacadeImpl implements ArticleQueryFacade {

    private final ArticleMapper articleMapper;

    @Override
    public ArticleSummary getById(Long id) {
        if (id == null) {
            return null;
        }
        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() != ArticleStatus.Published) {
            return null;
        }
        return toSummary(article);
    }

    @Override
    public List<ArticleSummary> searchByKeyword(String keyword) {
        LambdaQueryWrapper<Article> wrapper =
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, ArticleStatus.Published);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Article::getTitle, keyword);
        }
        List<Article> articles = articleMapper.selectList(wrapper);
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        return articles.stream().filter(Objects::nonNull).map(this::toSummary).collect(Collectors.toList());
    }

    private ArticleSummary toSummary(Article article) {
        return ArticleSummary.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .authorId(article.getAuthorId())
                .publishedAt(article.getPublishedAt())
                .build();
    }
}
