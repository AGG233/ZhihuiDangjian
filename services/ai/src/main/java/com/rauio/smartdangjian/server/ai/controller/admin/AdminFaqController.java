package com.rauio.smartdangjian.server.ai.controller.admin;

import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_NUM_MIN;
import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_SIZE_MAX;
import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_SIZE_MIN;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
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
@SaCheckRole(RoleConstants.MANAGER)
@Validated
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
            @RequestParam(defaultValue = "1") @Min(PAGE_NUM_MIN) int pageNum,
            @RequestParam(defaultValue = "10") @Min(PAGE_SIZE_MIN) @Max(PAGE_SIZE_MAX) int pageSize) {
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
