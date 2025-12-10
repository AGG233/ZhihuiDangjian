package com.rauio.ZhihuiDangjian.controller;

import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.dto.UserLearningRecordDto;
import com.rauio.ZhihuiDangjian.pojo.response.Result;
import com.rauio.ZhihuiDangjian.pojo.vo.UserLearningRecordVO;
import com.rauio.ZhihuiDangjian.service.UserLearningRecordService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户学习记录接口", description = "用户学习记录管理接口")
@RestController
@RequestMapping("/api/learning-record")
@RequiredArgsConstructor
public class UserLearningRecordController {

    private final UserLearningRecordService recordService;

    @Operation(summary = "获取学习记录", description = "根据记录ID获取学习记录详情")
    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    public Result<UserLearningRecordVO> get(@Parameter(description = "记录ID") @PathVariable Long id) {
        UserLearningRecordVO result = recordService.get(id);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户所有学习记录", description = "根据用户ID获取该用户的所有学习记录")
    @GetMapping("/user/{userId}")
    @PermissionAccess(UserType.STUDENT)
    public Result<List<UserLearningRecordVO>> getByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<UserLearningRecordVO> result = recordService.getByUserId(userId);
        return Result.ok(result);
    }

    @Operation(summary = "获取章节所有学习记录", description = "根据章节ID获取该章节的所有学习记录")
    @GetMapping("/chapter/{chapterId}")
    @PermissionAccess(UserType.SCHOOL)
    public Result<List<UserLearningRecordVO>> getByChapterId(@Parameter(description = "章节ID") @PathVariable Long chapterId) {
        List<UserLearningRecordVO> result = recordService.getByChapterId(chapterId);
        return Result.ok(result);
    }

    @Operation(summary = "获取用户章节学习记录", description = "获取指定用户在指定章节的所有学习记录")
    @GetMapping("/user/{userId}/chapter/{chapterId}")
    @PermissionAccess(UserType.STUDENT)
    public Result<List<UserLearningRecordVO>> getByUserAndChapter(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "章节ID") @PathVariable Long chapterId) {
        List<UserLearningRecordVO> result = recordService.getByUserAndChapter(userId, chapterId);
        return Result.ok(result);
    }

    @Operation(summary = "创建学习记录", description = "创建新的学习记录")
    @PostMapping("/")
    @PermissionAccess(UserType.STUDENT)
    public Result<Boolean> create(@RequestBody UserLearningRecordDto dto) {
        Boolean result = recordService.create(dto);
        return Result.ok(result);
    }

    @Operation(summary = "更新学习记录", description = "更新学习记录")
    @PutMapping("/")
    @PermissionAccess(UserType.STUDENT)
    public Result<Boolean> update(@RequestBody UserLearningRecordDto dto) {
        Boolean result = recordService.update(dto);
        return Result.ok(result);
    }

    @Operation(summary = "删除学习记录", description = "根据记录ID删除学习记录")
    @DeleteMapping("/{id}")
    @PermissionAccess(UserType.SCHOOL)
    public Result<Boolean> delete(@Parameter(description = "记录ID") @PathVariable Long id) {
        Boolean result = recordService.delete(id);
        return Result.ok(result);
    }
}
