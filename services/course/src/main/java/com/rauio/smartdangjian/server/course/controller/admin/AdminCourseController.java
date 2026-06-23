package com.rauio.smartdangjian.server.course.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.course.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.course.service.course.CourseService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员课程接口", description = "提供课程的管理功能")
@RestController
@RequestMapping("/api/admin/content/courses")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.SCHOOL)
@Validated
public class AdminCourseController {

    private static final int PAGE_NUM_MIN = 1;
    private static final int PAGE_SIZE_MIN = 1;
    private static final int PAGE_SIZE_MAX = 100;

    private final CourseService courseService;

    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息")
    @GetMapping("/{id}")
    public Result<CourseResponse> get(@Parameter(name = "id", description = "课程ID") @PathVariable Long id) {
        return Result.ok(courseService.get(id));
    }

    @Operation(summary = "分页查询课程列表", description = "管理员分页查询课程列表，支持按关键字/分类/难度/发布状态筛选。高校管理员仅能看到本校课程，系统管理员可查看全量。")
    @GetMapping
    public Result<Page<CourseResponse>> getPage(
            @Parameter(name = "keyword", description = "关键字搜索（标题/描述）") @RequestParam(required = false) String keyword,
            @Parameter(name = "categoryId", description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(name = "difficulty", description = "难度") @RequestParam(required = false) String difficulty,
            @Parameter(name = "isPublished", description = "发布状态") @RequestParam(required = false) Boolean isPublished,
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") @Min(PAGE_NUM_MIN)
                    int pageNum,
            @Parameter(name = "pageSize", description = "每页大小")
                    @RequestParam(defaultValue = "10")
                    @Min(PAGE_SIZE_MIN)
                    @Max(PAGE_SIZE_MAX)
                    int pageSize) {
        return Result.ok(
                courseService.searchAdminCourses(keyword, categoryId, difficulty, isPublished, pageNum, pageSize));
    }

    @Operation(summary = "更新课程信息", description = "根据课程ID更新课程信息")
    @PutMapping("/{id}")
    public Result<Void> update(@RequestBody @Valid CourseRequest course, @PathVariable Long id) {
        courseService.update(course, id);
        return Result.ok();
    }

    @Operation(summary = "创建课程", description = "创建一个新的课程")
    @PostMapping
    public Result<Void> create(@RequestBody @Valid CourseRequest course) {
        courseService.create(course);
        return Result.ok();
    }

    @Operation(summary = "删除课程", description = "根据课程ID删除指定课程")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.ok();
    }
}
