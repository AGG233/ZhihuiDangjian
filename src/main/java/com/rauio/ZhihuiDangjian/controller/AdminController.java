package com.rauio.ZhihuiDangjian.controller;


import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.service.AdminService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name= "系统管理员接口")
@RestController("/api/admin")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/user")
    @Operation(description = "添加用户，请求体为一个列表，列表元素为用户请求体")
    public ResponseEntity<String> addUser(@RequestBody List<UserDto> user){
        String result = adminService.addUser(user);
        return ApiResponse.buildResponse(result);
    }

    @DeleteMapping("/user")
    @Operation(description = "删除用户，请求体为一个用户ID列表")
    public ResponseEntity<String> deleteUser(@RequestBody List<String> idList) {
        String result = adminService.deleteUser(idList);
        return ApiResponse.buildResponse(result);
    }

    @PutMapping("/user")
    @Operation(description = "更新用户信息，请求体为一个列表，列表元素为用户请求体")
    public ResponseEntity<String> updateUser(@RequestBody List<UserDto> user) {
        String  result = adminService.updateUser(user);
        return ApiResponse.buildResponse(result);
    }


    @PostMapping("/school")
    @Operation(description = "添加高校管理员账号")
    public ResponseEntity<String> addSchoolAdmin(@RequestBody List<UserDto> user) {
        String  result = adminService.addSchoolAdmin(user);
        return ApiResponse.buildResponse(result);
    }
    @PutMapping("/school")
    @Operation(description = "修改高校管理员账号")
    public ResponseEntity<String> updateSchoolAdmin(@RequestBody List<UserDto> user) {
        String  result = adminService.updateSchoolAdmin(user);
        return ApiResponse.buildResponse(result);
    }
    @DeleteMapping("/school")
    @Operation(description = "添加高校管理员账号")
    public ResponseEntity<String> deleteSchoolAdmin(@RequestBody List<String> idList) {
        String  result = adminService.deleteSchoolAdmin(idList);
        return ApiResponse.buildResponse(result);
    }

}
