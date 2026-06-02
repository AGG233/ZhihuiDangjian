package com.rauio.smartdangjian.server.content.controller.admin;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.content.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.content.service.course.CourseService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员课程接口", description = "提供课程的管理功能")
@RestController
@RequestMapping("/api/admin/content/courses")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.SCHOOL)
public class AdminCourseController {

    private final CourseService courseService;

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
