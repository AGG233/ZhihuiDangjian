package com.rauio.smartdangjian.server.learning.controller.user;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.learning.pojo.request.UserChapterProgressRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户章节进度接口", description = "用户章节学习进度管理接口")
@RestController
@RequestMapping("/api/learning/progress")
@RequiredArgsConstructor
public class UserChapterProgressController {

    private final UserChapterProgressService progressService;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "获取当前用户进度记录", description = "根据进度ID获取当前用户章节进度记录")
    @GetMapping("/me/{id}")
    @SaCheckRole("STUDENT")
    public Result<UserChapterProgressResponse> get(
            @Parameter(name = "id", description = "进度ID") @PathVariable Long id) {
        UserChapterProgressResponse result = progressService.getForUser(id, currentUserId());
        return Result.ok(result);
    }

    @Operation(summary = "获取当前用户所有进度", description = "获取当前用户的所有章节进度")
    @GetMapping("/me")
    @SaCheckRole("STUDENT")
    public Result<List<UserChapterProgressResponse>> getMine() {
        List<UserChapterProgressResponse> result = progressService.getByUserId(currentUserId());
        return Result.ok(result);
    }

    @Operation(summary = "获取当前用户章节进度", description = "获取当前用户在指定章节的学习进度")
    @GetMapping("/me/chapters/{chapterId}")
    @SaCheckRole("STUDENT")
    public Result<UserChapterProgressResponse> getMineByChapterId(
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable Long chapterId) {
        UserChapterProgressResponse result = progressService.getByUserIdAndChapterId(currentUserId(), chapterId);
        return Result.ok(result);
    }

    @Operation(summary = "创建进度记录", description = "创建新的用户章节进度记录")
    @PostMapping
    @SaCheckRole("STUDENT")
    public Result<Boolean> create(@RequestBody @Valid UserChapterProgressRequest dto) {
        Boolean result = progressService.createForUser(dto, currentUserId());
        return Result.ok(result);
    }

    @Operation(summary = "更新进度记录", description = "更新用户章节进度记录")
    @PutMapping
    @SaCheckRole("STUDENT")
    public Result<Boolean> update(@RequestBody @Valid UserChapterProgressRequest dto) {
        Boolean result = progressService.updateForUser(dto, currentUserId());
        return Result.ok(result);
    }

    private Long currentUserId() {
        return Long.valueOf(currentUserProvider.getCurrentUserId());
    }
}
