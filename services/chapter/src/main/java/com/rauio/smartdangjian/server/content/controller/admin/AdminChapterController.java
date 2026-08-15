package com.rauio.smartdangjian.server.content.controller.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员章节接口", description = "提供课程章节的管理功能")
@RestController
@RequestMapping("/api/admin/content/chapters")
@RequiredArgsConstructor
@SaCheckRole("SCHOOL")
public class AdminChapterController {
    private final ChapterService chapterService;

    @Operation(summary = "获取章节详情", description = "根据章节ID获取章节详情")
    @GetMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.CHAPTER_ADMIN, action = DataScopeAction.READ, id = "#id")
    public Result<ChapterResponse> get(@PathVariable Long id) {
        return Result.ok(chapterService.get(id));
    }

    @Operation(summary = "获取课程下的章节列表", description = "根据课程ID获取该课程下的所有章节列表")
    @GetMapping("/by-course/{courseId}")
    @DataScopeAccess(resource = DataScopeResources.CHAPTER_ADMIN, action = DataScopeAction.READ, id = "#courseId")
    public Result<List<ChapterResponse>> getByCourseId(@PathVariable Long courseId) {
        return Result.ok(chapterService.getByCourseId(courseId));
    }

    @Operation(summary = "创建章节", description = "具体在Schema看每个字段的作用")
    @PostMapping
    @DataScopeAccess(resource = DataScopeResources.CHAPTER_ADMIN, action = DataScopeAction.CREATE, body = "#chapter")
    public Result<Boolean> create(@RequestBody @Valid ChapterRequest chapter) {
        Boolean result = chapterService.create(chapter);
        return Result.ok(result);
    }

    @Operation(summary = "更新章节")
    @PutMapping("/{id}")
    @DataScopeAccess(
            resource = DataScopeResources.CHAPTER_ADMIN,
            action = DataScopeAction.UPDATE,
            id = "#id",
            body = "#chapter")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ChapterRequest chapter) {
        chapterService.update(chapter, id);
        return Result.ok();
    }

    @Operation(summary = "删除章节", description = "根据章节ID删除章节")
    @DeleteMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.CHAPTER_ADMIN, action = DataScopeAction.DELETE, id = "#id")
    public Result<Void> delete(@PathVariable Long id) {
        chapterService.delete(id);
        return Result.ok();
    }
}
