package com.rauio.smartdangjian.server.ai.controller.admin;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiSkill;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillUpdateRequest;
import com.rauio.smartdangjian.server.ai.service.SkillService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI技能接口", description = "提供AI技能管理功能")
@RestController
@RequestMapping("/api/admin/ai/skills")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
public class AdminSkillController {

    private final SkillService skillService;

    @Operation(summary = "创建技能")
    @PostMapping
    public Result<AiSkill> create(@RequestBody @Valid AiSkillCreateRequest request) {
        return Result.ok(skillService.create(request));
    }

    @Operation(summary = "获取技能")
    @GetMapping("/{id}")
    public Result<AiSkill> get(@PathVariable String id) {
        return Result.ok(skillService.getById(id));
    }

    @Operation(summary = "查询技能")
    @GetMapping
    public Result<List<AiSkill>> list() {
        return Result.ok(skillService.list());
    }

    @Operation(summary = "更新技能")
    @PutMapping("/{id}")
    public Result<AiSkill> update(@PathVariable String id, @RequestBody @Valid AiSkillUpdateRequest request) {
        return Result.ok(skillService.update(id, request));
    }

    @Operation(summary = "删除技能")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(skillService.removeById(id));
    }
}
