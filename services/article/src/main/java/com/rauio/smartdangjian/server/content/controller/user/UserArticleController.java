package com.rauio.smartdangjian.server.content.controller.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.response.ArticleResponse;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户文章接口", description = "用户查看文章详情、分页列表与分类文章")
@RestController
@RequestMapping("/api/content/articles")
@RequiredArgsConstructor
@SaCheckRole("STUDENT")
public class UserArticleController {

    private final ArticleService articleService;

    @Operation(summary = "获取文章详情", description = "根据文章ID获取文章详情，包含内容块与分类ID")
    @GetMapping("/{id}")
    public Result<ArticleResponse> getArticle(@Parameter(name = "id", description = "文章ID") @PathVariable Long id) {
        return Result.ok(articleService.getDetail(id));
    }

    @Operation(summary = "分页获取文章列表", description = "按分页参数获取文章列表")
    @GetMapping
    public Result<List<Article>> getArticles(
            @Parameter(name = "pageNum", description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "pageSize", description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(articleService.getPage(pageNum, pageSize));
    }

    @Operation(summary = "获取分类下文章", description = "根据分类ID获取该分类下的完整文章列表")
    @GetMapping("/by-category/{categoryId}")
    public Result<List<Article>> getArticlesByCategory(
            @Parameter(name = "categoryId", description = "分类ID") @PathVariable Long categoryId) {
        return Result.ok(articleService.getArticlesByCategoryId(categoryId));
    }
}
