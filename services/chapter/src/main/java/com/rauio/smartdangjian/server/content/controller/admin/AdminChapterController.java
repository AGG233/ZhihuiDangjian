package com.rauio.smartdangjian.server.content.controller.admin;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员章节接口", description = "提供课程章节的管理功能")
@RestController
@RequestMapping("/api/admin/content/chapters")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
public class AdminChapterController {
    private final ChapterService chapterService;

    @Operation(summary = "创建章节", description = "具体在Schema看每个字段的作用")
    @PostMapping
    @DataScopeAccess(resource = DataScopeResources.CHAPTER_ADMIN, action = DataScopeAction.CREATE, body = "#chapter")
    public Result<Boolean> create(@RequestBody @Valid ChapterDto chapter){
        Boolean result = chapterService.create(chapter);
        return Result.ok(result);
    }

    @Operation(summary = "更新章节")
    @PutMapping
    @DataScopeAccess(resource = DataScopeResources.CHAPTER_ADMIN, action = DataScopeAction.UPDATE, body = "#chapter")
    public Result<Boolean> update(@RequestBody ChapterDto chapter){
        Boolean result = chapterService.update(chapter);
        return Result.ok(result);
    }

    @Operation(summary = "删除章节", description = "根据章节ID删除章节")
    @DeleteMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.CHAPTER_ADMIN, action = DataScopeAction.DELETE, id = "#id")
    public Result<Boolean> delete(@PathVariable String id){
        Boolean result = chapterService.delete(id);
        return Result.ok(result);
    }
}
