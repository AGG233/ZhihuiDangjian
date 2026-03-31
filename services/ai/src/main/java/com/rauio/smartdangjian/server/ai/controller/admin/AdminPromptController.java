package com.rauio.smartdangjian.server.ai.controller.admin;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiPrompts;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptUpdateRequest;
import com.rauio.smartdangjian.server.ai.service.PromptService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI提示词接口", description = "提供AI系统提示词管理功能")
@RestController
@RequestMapping("/api/admin/ai/prompts")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
public class AdminPromptController {

    private final PromptService promptService;

    @Operation(summary = "创建系统提示词", description = "新增AI系统提示词")
    @PostMapping
    public Result<AiPrompts> create(@RequestBody @Valid AiPromptCreateRequest request) {
        return Result.ok(promptService.create(request));
    }

    @Operation(summary = "获取系统提示词", description = "根据ID获取AI系统提示词")
    @GetMapping("/{id}")
    public Result<AiPrompts> get(@PathVariable String id) {
        return Result.ok(promptService.getById(id));
    }

    @Operation(summary = "查询系统提示词", description = "查询全部提示词")
    @GetMapping
    public Result<List<AiPrompts>> list() {
        return Result.ok(promptService.list());
    }

    @Operation(summary = "更新系统提示词", description = "根据ID更新AI系统提示词")
    @PutMapping("/{id}")
    public Result<AiPrompts> update(@PathVariable String id, @RequestBody @Valid AiPromptUpdateRequest request) {
        return Result.ok(promptService.update(id, request));
    }

    @Operation(summary = "删除系统提示词", description = "根据ID删除AI系统提示词")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(promptService.removeById(id));
    }
}
