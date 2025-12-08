package com.rauio.ZhihuiDangjian.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.service.AdminService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name= "系统管理员接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/user/{pageNum}/{pageSize}")
    @Operation(description = "获取普通用户和高校管理员信息，请求体为用户请求体")
    public ApiResponse<Page<User>> getUser(@RequestParam UserDto userDto, @PathVariable int pageNum, @PathVariable int pageSize) {
        Page<User> result = adminService.getUser(userDto, pageNum, pageSize);
        return ApiResponse.ok(result);
    }

    @PutMapping("/user")
    @Operation(description = "更新普通用户和高校管理员信息，请求体为一个列表，列表元素为用户请求体")
    public ApiResponse<String> updateUser(@RequestBody List<UserDto> user) {
        String  result = adminService.updateUser(user);
        return ApiResponse.ok(result);
    }

    @PostMapping("/user")
    @Operation(description = "添加用户，请求体为一个列表，列表元素为用户请求体")
    public ApiResponse<String> addUser(@RequestBody List<UserDto> user){
        String result = adminService.addUser(user);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/user")
    @Operation(description = "删除用户，请求体为一个用户ID列表")
    public ApiResponse<String> deleteUser(@RequestBody List<String> idList) {
        String result = adminService.deleteUser(idList);
        return ApiResponse.ok(result);
    }

    @PostMapping("/school")
    @Operation(description = "添加高校管理员账号")
    public ApiResponse<String> addSchoolAdmin(@RequestBody List<UserDto> user) {
        String  result = adminService.addSchoolAdmin(user);
        return ApiResponse.ok(result);
    }
    @PutMapping("/school")
    @Operation(description = "修改高校管理员账号")
    public ApiResponse<String> updateSchoolAdmin(@RequestBody List<UserDto> user) {
        String  result = adminService.updateSchoolAdmin(user);
        return ApiResponse.ok(result);
    }
    @DeleteMapping("/school")
    @Operation(description = "添加高校管理员账号")
    public ApiResponse<String> deleteSchoolAdmin(@RequestBody List<String> idList) {
        String  result = adminService.deleteSchoolAdmin(idList);
        return ApiResponse.ok(result);
    }

}
