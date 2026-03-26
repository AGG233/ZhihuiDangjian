package com.rauio.smartdangjian.server.content.controller.admin;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.server.content.pojo.dto.CourseDto;
import com.rauio.smartdangjian.server.content.pojo.request.PageRequest;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.server.content.pojo.vo.PageVO;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员课程接口", description = "提供课程的管理功能")
@RestController
@RequestMapping("/api/admin/content/courses")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
public class AdminCourseController {

    private final CourseService     courseService;

    @Operation(summary = "更新课程信息", description = "根据课程ID更新课程信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@RequestBody CourseDto course, @PathVariable String id) throws JsonProcessingException {
        Boolean result = courseService.update(course,id);
        return Result.ok(result);
    }

    @Operation(summary = "创建课程", description = "创建一个新的课程")
    @PostMapping("/")
    public Result<Boolean> create(@RequestBody CourseDto course) throws JsonProcessingException {
        Boolean result = courseService.create(course);
        return Result.ok(result);
    }

    @Operation(summary = "删除课程", description = "根据课程ID删除指定课程")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) throws JsonProcessingException {
        Boolean result = courseService.delete(id);
        return Result.ok(result);
    }
}
