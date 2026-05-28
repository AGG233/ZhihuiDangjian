package com.rauio.smartdangjian.server.learning.controller.user;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.utils.spec.UserType;

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

    @Operation(summary = "获取学习记录", description = "根据记录ID获取学习记录详情")
    @GetMapping("/{id}")
    @SaCheckRole("STUDENT")
    public Result<UserLearningRecordResponse> get(
            @Parameter(name = "id", description = "记录ID") @PathVariable Long id) {
        UserLearningRecordResponse result = recordService.get(id);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户所有学习记录", description = "根据用户ID获取该用户的所有学习记录")
    @GetMapping("/users/{userId}")
    @SaCheckRole("STUDENT")
    public Result<List<UserLearningRecordResponse>> getByUserId(
            @Parameter(name = "userId", description = "用户ID") @PathVariable Long userId) {
        List<UserLearningRecordResponse> result = recordService.getByUserId(userId);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户章节学习记录", description = "获取指定用户在指定章节的所有学习记录")
    @GetMapping("/users/{userId}/chapters/{chapterId}")
    @SaCheckRole("STUDENT")
    public Result<List<UserLearningRecordResponse>> getByUserIdAndChapterId(
            @Parameter(name = "userId", description = "用户ID") @PathVariable Long userId,
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable Long chapterId) {
        List<UserLearningRecordResponse> result = recordService.getByUserIdAndChapterId(userId, chapterId);
        return Result.ok(result);
    }

    @Operation(summary = "创建学习记录", description = "创建新的学习记录")
    @PostMapping
    @SaCheckRole("STUDENT")
    public Result<Boolean> create(@RequestBody @Valid UserLearningRecordRequest dto) {
        Boolean result = recordService.create(dto);
        return Result.ok(result);
    }

    @Operation(summary = "更新学习记录", description = "更新学习记录")
    @PutMapping
    @SaCheckRole("STUDENT")
    public Result<Boolean> update(@RequestBody @Valid UserLearningRecordRequest dto) {
        Boolean result = recordService.update(dto);
        return Result.ok(result);
    }
}
