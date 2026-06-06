package com.rauio.smartdangjian.server.content.controller.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterContentBlockRequest;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员内容接口", description = "提供轮播图等内容块管理能力")
@RestController
@RequestMapping("/api/admin/content/content-blocks")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.MANAGER)
public class AdminContentController {

    private final ChapterContentBlockService chapterContentBlockService;

    @Operation(summary = "更新轮播图")
    @PutMapping("/carousel")
    public Result<Boolean> updateCarousel(@RequestBody @Valid ChapterContentBlockRequest request) {
        return Result.ok(chapterContentBlockService.updateCarousel(request));
    }

    @Operation(summary = "添加轮播图")
    @PostMapping("/carousel")
    public Result<Boolean> addCarousel(@RequestBody @Valid List<ChapterContentBlockRequest> requests) {
        return Result.ok(chapterContentBlockService.createCarouselBatch(requests));
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/carousel/{id}")
    public Result<Boolean> deleteCarousel(@PathVariable Long id) {
        return Result.ok(chapterContentBlockService.delete(id));
    }
}
