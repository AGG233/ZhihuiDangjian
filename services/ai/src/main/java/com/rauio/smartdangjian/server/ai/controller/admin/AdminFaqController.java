package com.rauio.smartdangjian.server.ai.controller.admin;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.ai.pojo.request.FaqCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.FaqUpdateRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiFaqResponse;
import com.rauio.smartdangjian.server.ai.service.FaqService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "AI-FAQ管理接口", description = "提供AI FAQ快速回复规则管理功能")
@RestController
@RequestMapping("/api/admin/ai/faqs")
@RequiredArgsConstructor
@SaCheckRole("MANAGER")
public class AdminFaqController {

    private final FaqService faqService;

    @Operation(summary = "创建FAQ", description = "新增AI FAQ快速回复规则")
    @PostMapping
    public Result<AiFaqResponse> create(@RequestBody @Valid FaqCreateRequest request) {
        return Result.ok(faqService.createFaq(request));
    }

    @Operation(summary = "获取FAQ", description = "根据ID获取FAQ详情")
    @GetMapping("/{id}")
    public Result<AiFaqResponse> get(@PathVariable Long id) {
        return Result.ok(faqService.getFaqResponse(id));
    }

    @Operation(summary = "分页查询FAQ", description = "分页查询FAQ列表，按sort升序")
    @GetMapping
    public Result<IPage<AiFaqResponse>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(faqService.pageFaqs(pageNum, pageSize));
    }

    @Operation(summary = "更新FAQ", description = "根据ID更新FAQ规则")
    @PutMapping("/{id}")
    public Result<AiFaqResponse> update(@PathVariable Long id, @RequestBody @Valid FaqUpdateRequest request) {
        request.setId(id);
        return Result.ok(faqService.updateFaq(request));
    }

    @Operation(summary = "删除FAQ", description = "根据ID删除FAQ规则")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return Result.ok(true);
    }
}
