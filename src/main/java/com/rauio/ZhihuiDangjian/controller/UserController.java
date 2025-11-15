package com.rauio.ZhihuiDangjian.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.aop.annotation.ResourceAccess;
import com.rauio.ZhihuiDangjian.pojo.Course;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.UserVO;
import com.rauio.ZhihuiDangjian.service.CourseService;
import com.rauio.ZhihuiDangjian.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="用户管理接口", description = "提供用户信息操作")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final CourseService courseService;
    private final ObjectMapper objectMapper;


    @Operation(
            summary = "获取用户信息",
            description = "通过ID获取用户信息")
    @GetMapping("/{id}")
    public ResponseEntity<String> get(
            @Parameter(description = "用户ID") @PathVariable String id
    ) throws JsonProcessingException {
        UserVO user = userService.getUserByID(id);
        return ApiResponse.buildResponse(user);
    }

    @Operation(
            summary = "更新用户信息",
            description = "通过ID更新用户信息"
    )
    @PutMapping("/{id}")
    @ResourceAccess(id = "#id")
    public ResponseEntity<String> update(
            @PathVariable String id,
            @RequestBody User user
    ) throws JsonProcessingException {
        Boolean result = userService.update(id,user);
        return ApiResponse.buildResponse(result);
    }

    @Operation(
            summary = "删除用户",
            description = "通过ID删除用户"
    )
    @DeleteMapping("/{id}")
    @ResourceAccess(id = "#id")
    public ResponseEntity<String> delete(@Parameter(description = "用户ID") @PathVariable String id) throws JsonProcessingException {
        Boolean result = userService.delete(id);
        return ApiResponse.buildResponse(result);
    }

    @GetMapping("/course/{id}")
    public ResponseEntity<String> getAllCoursesOfUser(@PathVariable String id) throws JsonProcessingException {
        List<Course> result = courseService.getAllCoursesOfUser(id);
        return ApiResponse.buildResponse(result);
    }
}