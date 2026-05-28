package com.rauio.smartdangjian.server.learning.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.utils.spec.UserType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员学习记录接口")
@RestController
@RequestMapping("/api/admin/learning/records")
@RequiredArgsConstructor
@SaCheckRole("SCHOOL")
public class AdminLearningRecordController {

    private final UserLearningRecordService recordService;

    @Operation(summary = "获取章节所有学习记录")
    @GetMapping("/chapter/{chapterId}")
    public Result<List<UserLearningRecordResponse>> getByChapterId(
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable Long chapterId) {
        return Result.ok(recordService.getByChapterId(chapterId));
    }

    @Operation(summary = "删除学习记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@Parameter(name = "id", description = "记录ID") @PathVariable Long id) {
        return Result.ok(recordService.delete(id));
    }
}
