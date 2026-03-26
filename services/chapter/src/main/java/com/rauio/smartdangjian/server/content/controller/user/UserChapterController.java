package com.rauio.smartdangjian.server.content.controller.user;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户章节接口")
@RestController
@RequestMapping("/api/content/chapters")
@RequiredArgsConstructor
@PermissionAccess(UserType.STUDENT)
public class UserChapterController {

    private final ChapterService chapterService;

    @Operation(summary = "获取章节详情")
    @GetMapping("/{id}")
    public Result<ChapterVO> get(@PathVariable String id) {
        return Result.ok(chapterService.get(id));
    }

    @Operation(summary = "获取课程下的章节列表")
    @GetMapping("/by-course/{courseId}")
    public Result<List<ChapterVO>> getByCourseId(@PathVariable String courseId) {
        return Result.ok(chapterService.getByCourseId(courseId));
    }
}
