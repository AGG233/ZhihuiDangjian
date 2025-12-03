package com.rauio.ZhihuiDangjian.controller;

import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.dto.UserChapterProgressDto;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.UserChapterProgressVO;
import com.rauio.ZhihuiDangjian.service.UserChapterProgressService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户章节进度接口", description = "用户章节学习进度管理接口")
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class UserChapterProgressController {

    private final UserChapterProgressService progressService;

    @Operation(summary = "获取进度记录", description = "根据进度ID获取用户章节进度记录")
    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<UserChapterProgressVO> get(@Parameter(description = "进度ID") @PathVariable Long id) {
        UserChapterProgressVO result = progressService.get(id);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "获取用户所有进度", description = "根据用户ID获取该用户的所有章节进度")
    @GetMapping("/user/{userId}")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<List<UserChapterProgressVO>> getByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<UserChapterProgressVO> result = progressService.getByUserId(userId);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "获取章节所有进度", description = "根据章节ID获取该章节的所有用户进度")
    @GetMapping("/chapter/{chapterId}")
    @PermissionAccess(UserType.TEACHER)
    public ApiResponse<List<UserChapterProgressVO>> getByChapterId(@Parameter(description = "章节ID") @PathVariable Long chapterId) {
        List<UserChapterProgressVO> result = progressService.getByChapterId(chapterId);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "获取用户章节进度", description = "获取指定用户在指定章节的学习进度")
    @GetMapping("/user/{userId}/chapter/{chapterId}")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<UserChapterProgressVO> getByUserAndChapter(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "章节ID") @PathVariable Long chapterId) {
        UserChapterProgressVO result = progressService.getByUserAndChapter(userId, chapterId);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "创建进度记录", description = "创建新的用户章节进度记录")
    @PostMapping("/")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<Boolean> create(@RequestBody UserChapterProgressDto dto) {
        Boolean result = progressService.create(dto);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "更新进度记录", description = "更新用户章节进度记录")
    @PutMapping("/")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<Boolean> update(@RequestBody UserChapterProgressDto dto) {
        Boolean result = progressService.update(dto);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "删除进度记录", description = "根据进度ID删除用户章节进度记录")
    @DeleteMapping("/{id}")
    @PermissionAccess(UserType.TEACHER)
    public ApiResponse<Boolean> delete(@Parameter(description = "进度ID") @PathVariable Long id) {
        Boolean result = progressService.delete(id);
        return ApiResponse.ok(result);
    }
}
