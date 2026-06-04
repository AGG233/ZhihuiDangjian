package com.rauio.smartdangjian.server.course.controller.user;

import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_NUM_MIN;
import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_SIZE_MAX;
import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_SIZE_MIN;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.course.pojo.response.PageResponse;
import com.rauio.smartdangjian.server.course.service.course.CourseService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户课程接口", description = "从用户视角查询课程")
@RestController
@RequestMapping("/api/content/courses")
@RequiredArgsConstructor
@Validated
public class UserCourseController {

    private final CourseService courseService;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息")
    @GetMapping("/{id}")
    public Result<CourseResponse> get(@PathVariable Long id) throws JsonProcessingException {
        return Result.ok(courseService.get(id));
    }

    @Operation(summary = "分页获取课程", description = "根据分页参数获取课程列表")
    @GetMapping
    public Result<PageResponse<Object>> getPage(
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") @Min(PAGE_NUM_MIN)
                    int pageNum,
            @Parameter(name = "pageSize", description = "页的大小")
                    @RequestParam(defaultValue = "10")
                    @Min(PAGE_SIZE_MIN)
                    @Max(PAGE_SIZE_MAX)
                    int pageSize) {
        return Result.ok(courseService.getPage(pageNum, pageSize));
    }

    @Operation(summary = "获取用户已学习课程", description = "根据用户ID获取已学习课程列表")
    @GetMapping("/learned/me")
    @SaCheckRole(RoleConstants.STUDENT)
    @SaCheckPermission("course:read")
    public Result<List<CourseResponse>> getByUserIdCourses() {
        Long currentUserId = Long.valueOf(currentUserProvider.getCurrentUserId());
        return Result.ok(courseService.getByUserId(currentUserId).stream()
                .map(CourseResponse::from)
                .toList());
    }
}
