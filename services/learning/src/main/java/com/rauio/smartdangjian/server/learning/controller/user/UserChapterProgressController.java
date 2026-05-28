package com.rauio.smartdangjian.server.learning.controller.user;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.learning.pojo.request.UserChapterProgressRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import com.rauio.smartdangjian.utils.spec.UserType;

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

    @Operation(summary = "获取进度记录", description = "根据进度ID获取用户章节进度记录")
    @GetMapping("/{id}")
    @SaCheckRole("STUDENT")
    public Result<UserChapterProgressResponse> get(
            @Parameter(name = "id", description = "进度ID") @PathVariable Long id) {
        UserChapterProgressResponse result = progressService.get(id);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户所有进度", description = "根据用户ID获取该用户的所有章节进度")
    @GetMapping("/users/{userId}")
    @SaCheckRole("STUDENT")
    public Result<List<UserChapterProgressResponse>> getByUserId(
            @Parameter(name = "userId", description = "用户ID") @PathVariable Long userId) {
        List<UserChapterProgressResponse> result = progressService.getByUserId(userId);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户章节进度", description = "获取指定用户在指定章节的学习进度")
    @GetMapping("/users/{userId}/chapters/{chapterId}")
    @SaCheckRole("STUDENT")
    public Result<UserChapterProgressResponse> getByUserIdAndChapterId(
            @Parameter(name = "userId", description = "用户ID") @PathVariable Long userId,
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable Long chapterId) {
        UserChapterProgressResponse result = progressService.getByUserIdAndChapterId(userId, chapterId);
        return Result.ok(result);
    }

    @Operation(summary = "创建进度记录", description = "创建新的用户章节进度记录")
    @PostMapping
    @SaCheckRole("STUDENT")
    public Result<Boolean> create(@RequestBody @Valid UserChapterProgressRequest dto) {
        Boolean result = progressService.create(dto);
        return Result.ok(result);
    }

    @Operation(summary = "更新进度记录", description = "更新用户章节进度记录")
    @PutMapping
    @SaCheckRole("STUDENT")
    public Result<Boolean> update(@RequestBody @Valid UserChapterProgressRequest dto) {
        Boolean result = progressService.update(dto);
        return Result.ok(result);
    }
}
