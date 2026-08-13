package com.rauio.smartdangjian.server.task.controller.admin;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.task.pojo.request.TaskCreateRequest;
import com.rauio.smartdangjian.server.task.pojo.request.TaskUpdateRequest;
import com.rauio.smartdangjian.server.task.pojo.response.TaskResponse;
import com.rauio.smartdangjian.server.task.service.AdminTaskService;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员任务接口", description = "任务的发布管理，包含状态流转 draft→published→closed，需要校级或管理员权限")
@RestController
@RequestMapping("/api/admin/tasks")
@RequiredArgsConstructor
@SaCheckRole(
        value = {"SCHOOL", "MANAGER"},
        mode = SaMode.OR)
public class AdminTaskController {

    private final AdminTaskService adminTaskService;

    @Operation(summary = "创建任务", description = "创建任务，初始状态为草稿（draft），需通过更新接口发布")
    @PostMapping
    public Result<TaskResponse> create(@RequestBody @Valid TaskCreateRequest request) {
        return Result.ok(adminTaskService.create(request));
    }

    @Operation(summary = "更新任务", description = "更新任务信息，支持状态流转 draft→published→closed")
    @PutMapping("/{id}")
    public Result<TaskResponse> update(
            @Parameter(name = "id", description = "任务ID") @PathVariable Long id,
            @RequestBody @Valid TaskUpdateRequest request) {
        return Result.ok(adminTaskService.update(id, request));
    }

    @Operation(summary = "删除任务", description = "根据ID删除任务，并级联清理该任务的全部领取记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(name = "id", description = "任务ID") @PathVariable Long id) {
        adminTaskService.delete(id);
        return Result.ok();
    }
}
