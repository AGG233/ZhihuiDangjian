package com.rauio.ZhihuiDangjian.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="用户管理接口", description = "提供用户信息操作")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final CourseService courseService;

    @Operation(
            summary = "获取用户信息",
            description = "通过ID获取用户信息")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> get(@Parameter(description = "用户ID") @PathVariable String id){
        UserVO user = userService.getUserByID(id);
        return ApiResponse.ok(user);
    }

    @Operation(
            summary = "更新用户信息",
            description = "通过ID更新用户信息"
    )
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(
            @PathVariable String id,
            @RequestBody User user
    ){
        Boolean result = userService.update(id,user);
        return ApiResponse.ok(result);
    }

    @Operation(
            summary = "删除用户（已经弃用）",
            description = "通过ID删除用户"
    )
    @DeleteMapping("/{id}")
    @Deprecated
    public ApiResponse<Object> delete(@Parameter(description = "用户ID") @PathVariable String id){
//        Boolean result = userService.delete(id);
        return ApiResponse.ok("404","接口已经弃用",null);
    }

    @GetMapping("/course/{id}")
    public ApiResponse<List<Course>> getAllCoursesOfUser(@PathVariable String id){
        List<Course> result = courseService.getAllCoursesOfUser(id);
        return ApiResponse.ok(result);
    }
}