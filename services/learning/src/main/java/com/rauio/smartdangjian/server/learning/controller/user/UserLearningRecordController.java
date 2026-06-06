package com.rauio.smartdangjian.server.learning.controller.user;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户学习记录接口", description = "用户学习记录管理接口")
@RestController
@RequestMapping("/api/learning/records")
@RequiredArgsConstructor
public class UserLearningRecordController {

    private final UserLearningRecordService recordService;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "获取当前用户学习记录", description = "根据记录ID获取当前用户学习记录详情")
    @GetMapping("/me/{id}")
    @SaCheckRole(RoleConstants.STUDENT)
    public Result<UserLearningRecordResponse> get(@Parameter(name = "id", description = "记录ID") @PathVariable Long id) {
        UserLearningRecordResponse result = recordService.getForUser(id, currentUserId());
        return Result.ok(result);
    }

    @Operation(summary = "获取当前用户所有学习记录", description = "获取当前登录用户的所有学习记录")
    @GetMapping("/me")
    @SaCheckRole(RoleConstants.STUDENT)
    public Result<List<UserLearningRecordResponse>> getMine() {
        List<UserLearningRecordResponse> result = recordService.getByUserId(currentUserId());
        return Result.ok(result);
    }

    @Operation(summary = "获取当前用户章节学习记录", description = "获取当前用户在指定章节的所有学习记录")
    @GetMapping("/me/chapters/{chapterId}")
    @SaCheckRole(RoleConstants.STUDENT)
    public Result<List<UserLearningRecordResponse>> getMineByChapterId(
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable Long chapterId) {
        List<UserLearningRecordResponse> result = recordService.getByUserIdAndChapterId(currentUserId(), chapterId);
        return Result.ok(result);
    }

    @Operation(summary = "创建学习记录", description = "创建新的学习记录")
    @PostMapping
    @SaCheckRole(RoleConstants.STUDENT)
    public Result<Boolean> create(@RequestBody @Valid UserLearningRecordRequest dto) {
        Boolean result = recordService.createForUser(dto, currentUserId());
        return Result.ok(result);
    }

    @Operation(summary = "更新学习记录", description = "更新学习记录")
    @PutMapping
    @SaCheckRole(RoleConstants.STUDENT)
    public Result<Boolean> update(@RequestBody @Valid UserLearningRecordRequest dto) {
        Boolean result = recordService.updateForUser(dto, currentUserId());
        return Result.ok(result);
    }

    private Long currentUserId() {
        return Long.valueOf(currentUserProvider.getCurrentUserId());
    }
}
