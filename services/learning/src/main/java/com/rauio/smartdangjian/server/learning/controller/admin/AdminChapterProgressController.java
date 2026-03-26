package com.rauio.smartdangjian.server.learning.controller.admin;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserChapterProgressVO;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员章节进度接口")
@RestController
@RequestMapping("/api/admin/learning/progress")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
public class AdminChapterProgressController {

    private final UserChapterProgressService progressService;

    @Operation(summary = "获取章节所有进度")
    @GetMapping("/chapter/{chapterId}")
    public Result<List<UserChapterProgressVO>> getByChapterId(@Parameter(description = "章节ID") @PathVariable String chapterId) {
        return Result.ok(progressService.getByChapterId(chapterId));
    }

    @Operation(summary = "删除进度记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@Parameter(description = "进度ID") @PathVariable String id) {
        return Result.ok(progressService.delete(id));
    }
}
