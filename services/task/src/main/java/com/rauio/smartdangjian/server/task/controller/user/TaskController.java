package com.rauio.smartdangjian.server.task.controller.user;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.task.pojo.request.TaskSubmitRequest;
import com.rauio.smartdangjian.server.task.pojo.response.TaskAcceptanceResponse;
import com.rauio.smartdangjian.server.task.pojo.response.TaskPageResponse;
import com.rauio.smartdangjian.server.task.service.TaskService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户任务接口", description = "已发布任务的查看、领取与提交")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@SaCheckRole("STUDENT")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "分页查询已发布任务", description = "仅返回已发布（published）的任务，按创建时间倒序")
    @GetMapping
    public Result<TaskPageResponse> getPublishedPage(
            @Parameter(name = "pageNum", description = "页码，默认1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "pageSize", description = "每页大小，默认10") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(taskService.getPublishedPage(pageNum, pageSize));
    }

    @Operation(summary = "领取任务", description = "领取已发布任务，同一用户同一任务不可重复领取")
    @PostMapping("/{id}/accept")
    public Result<TaskAcceptanceResponse> accept(@Parameter(name = "id", description = "任务ID") @PathVariable Long id) {
        return Result.ok(taskService.accept(id));
    }

    @Operation(summary = "提交任务", description = "提交任务完成情况，包含进度更新（0-100），达到100视为完成")
    @PostMapping("/{id}/submit")
    public Result<TaskAcceptanceResponse> submit(
            @Parameter(name = "id", description = "任务ID") @PathVariable Long id,
            @RequestBody @Valid TaskSubmitRequest request) {
        return Result.ok(taskService.submit(id, request));
    }
}
