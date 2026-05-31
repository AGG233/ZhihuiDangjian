package com.rauio.smartdangjian.server.content.controller.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员内容接口", description = "提供轮播图等内容块管理能力")
@RestController
@RequestMapping("/api/admin/content/content-blocks")
@RequiredArgsConstructor
@SaCheckRole("MANAGER")
public class AdminContentController {

    private static final Long CAROUSEL_PARENT_ID = 1145141919810L;

    private final ChapterContentBlockService chapterContentBlockService;

    @Operation(summary = "更新轮播图")
    @PutMapping("/carousel")
    public Result<Boolean> updateCarousel(@RequestBody @Valid ChapterContentBlock chapterContentBlock) {
        return Result.ok(chapterContentBlockService.update(chapterContentBlock));
    }

    @Operation(summary = "添加轮播图")
    @PostMapping("/carousel")
    public Result<Boolean> addCarousel(@RequestBody @Valid List<ChapterContentBlock> chapterContentBlocks) {
        for (ChapterContentBlock block : chapterContentBlocks) {
            block.setChapterId(CAROUSEL_PARENT_ID);
        }
        return Result.ok(chapterContentBlockService.createBatch(chapterContentBlocks));
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/carousel/{id}")
    public Result<Boolean> deleteCarousel(@PathVariable Long id) {
        return Result.ok(chapterContentBlockService.delete(id));
    }
}
