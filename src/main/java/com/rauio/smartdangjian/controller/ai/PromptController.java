package com.rauio.smartdangjian.controller.ai;

import com.rauio.smartdangjian.pojo.AiSystemPrompt;
import com.rauio.smartdangjian.pojo.request.AiPromptCreateRequest;
import com.rauio.smartdangjian.pojo.request.AiPromptUpdateRequest;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.service.ai.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI提示词接口", description = "提供AI系统提示词管理功能")
@RestController
@RequestMapping("/ai/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    @Operation(summary = "创建系统提示词", description = "新增AI系统提示词")
    @PostMapping
    public Result<AiSystemPrompt> create(@RequestBody @Valid AiPromptCreateRequest request) {
        return Result.ok(promptService.create(request));
    }

    @Operation(summary = "获取系统提示词", description = "根据ID获取AI系统提示词")
    @GetMapping("/{id}")
    public Result<AiSystemPrompt> get(@PathVariable String id) {
        return Result.ok(promptService.get(id));
    }

    @Operation(summary = "查询系统提示词", description = "根据类型/状态查询AI系统提示词")
    @GetMapping
    public Result<List<AiSystemPrompt>> list(@RequestParam(required = false) String type,
                                             @RequestParam(required = false) Boolean enabled) {
        return Result.ok(promptService.list(type, enabled));
    }

    @Operation(summary = "更新系统提示词", description = "根据ID更新AI系统提示词")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable String id, @RequestBody @Valid AiPromptUpdateRequest request) {
        return Result.ok(promptService.update(id, request));
    }

    @Operation(summary = "删除系统提示词", description = "根据ID删除AI系统提示词")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(promptService.delete(id));
    }
}
