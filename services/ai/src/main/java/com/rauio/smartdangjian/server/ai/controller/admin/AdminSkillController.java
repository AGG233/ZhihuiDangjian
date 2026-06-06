package com.rauio.smartdangjian.server.ai.controller.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillUpdateRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiSkillResponse;
import com.rauio.smartdangjian.server.ai.service.SkillService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "AI技能接口", description = "提供AI技能管理功能")
@RestController
@RequestMapping("/api/admin/ai/skills")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.MANAGER)
public class AdminSkillController {

    private final SkillService skillService;

    @Operation(summary = "创建技能")
    @PostMapping
    public Result<AiSkillResponse> create(@RequestBody @Valid AiSkillCreateRequest request) {
        return Result.ok(AiSkillResponse.from(skillService.create(request)));
    }

    @Operation(summary = "获取技能")
    @GetMapping("/{id}")
    public Result<AiSkillResponse> get(@PathVariable String id) {
        return Result.ok(AiSkillResponse.from(skillService.getById(id)));
    }

    @Operation(summary = "查询技能")
    @GetMapping
    public Result<List<AiSkillResponse>> list() {
        return Result.ok(skillService.list().stream().map(AiSkillResponse::from).toList());
    }

    @Operation(summary = "更新技能")
    @PutMapping("/{id}")
    public Result<AiSkillResponse> update(@PathVariable String id, @RequestBody @Valid AiSkillUpdateRequest request) {
        return Result.ok(AiSkillResponse.from(skillService.update(id, request)));
    }

    @Operation(summary = "删除技能")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(skillService.removeById(id));
    }
}
