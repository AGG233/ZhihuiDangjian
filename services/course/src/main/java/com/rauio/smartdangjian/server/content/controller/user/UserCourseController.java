package com.rauio.smartdangjian.server.content.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.server.content.pojo.vo.PageVO;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户课程接口", description = "从用户视角查询课程")
@RestController
@RequestMapping("/api/content/courses")
@RequiredArgsConstructor
public class UserCourseController {

    private final CourseService courseService;

    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息")
    @GetMapping("/{id}")
    public Result<CourseVO> get(@PathVariable String id) throws JsonProcessingException {
        return Result.ok(courseService.get(id));
    }

    @Operation(summary = "分页获取课程", description = "根据分页参数获取课程列表")
    @GetMapping
    public Result<PageVO<Object>> getPage(
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "pageSize", description = "页的大小") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(courseService.getPage(pageNum, pageSize));
    }

    @Operation(summary = "获取用户已学习课程", description = "根据用户ID获取已学习课程列表")
    @GetMapping("/learned/{id}")
    public Result<List<Course>> getByUserIdCourses(@PathVariable String id) {
        return Result.ok(courseService.getByUserId(id));
    }
}
