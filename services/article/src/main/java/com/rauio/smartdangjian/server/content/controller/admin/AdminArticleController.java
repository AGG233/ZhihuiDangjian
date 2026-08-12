package com.rauio.smartdangjian.server.content.controller.admin;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.request.ArticleRequest;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员文章接口", description = "文章的增删改管理，创建/更新时连带维护分类关联与内容块，需要校级及以上权限")
@RestController
@RequestMapping("/api/admin/content/articles")
@RequiredArgsConstructor
@SaCheckRole("SCHOOL")
public class AdminArticleController {

    private final ArticleService articleService;

    @Operation(summary = "创建文章", description = "创建文章并落分类关联与内容块列表")
    @PostMapping
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.READ, id = "#request.categoryId")
    public Result<Void> create(@RequestBody @Valid ArticleRequest request) {
        articleService.create(request);
        return Result.ok();
    }

    @Operation(summary = "更新文章", description = "根据文章ID更新文章，内容块全量替换")
    @PutMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.ARTICLE_ADMIN, action = DataScopeAction.UPDATE, id = "#id")
    public Result<Void> update(
            @Parameter(name = "id", description = "文章ID") @PathVariable String id,
            @RequestBody @Valid ArticleRequest request) {
        request.setId(IdUtil.parse(id));
        articleService.update(request);
        return Result.ok();
    }

    @Operation(summary = "删除文章", description = "根据文章ID删除文章，级联清理分类关联与内容块")
    @DeleteMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.ARTICLE_ADMIN, action = DataScopeAction.DELETE, id = "#id")
    public Result<Void> delete(@Parameter(name = "id", description = "文章ID") @PathVariable String id) {
        articleService.delete(IdUtil.parse(id));
        return Result.ok();
    }
}
