package com.rauio.smartdangjian.server.learning.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import com.rauio.smartdangjian.utils.spec.UserType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员章节进度接口")
@RestController
@RequestMapping("/api/admin/learning/progress")
@RequiredArgsConstructor
@SaCheckRole("SCHOOL")
public class AdminChapterProgressController {

    private final UserChapterProgressService progressService;

    @Operation(summary = "获取章节所有进度")
    @GetMapping("/chapter/{chapterId}")
    public Result<List<UserChapterProgressResponse>> getByChapterId(
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable Long chapterId) {
        return Result.ok(progressService.getByChapterId(chapterId));
    }

    @Operation(summary = "删除进度记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@Parameter(name = "id", description = "进度ID") @PathVariable Long id) {
        return Result.ok(progressService.delete(id));
    }
}
