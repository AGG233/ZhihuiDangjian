package com.rauio.ZhihuiDangjian.controller;


import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.service.SchoolAdminService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/school/admin")
@RequiredArgsConstructor
@PermissionAccess(UserType.TEACHER)
@Slf4j
@Tag(name = "学校管理员接口")
public class SchoolAdminController {

    private final SchoolAdminService schoolAdminService;

    @PostMapping("/user")
    @Operation(summary = "添加用户",description = "添加用户")
    public ApiResponse<Integer> addUser(@RequestBody List<UserDto> userDtoList) {
        int result = schoolAdminService.addUser(userDtoList);
        return ApiResponse.ok(result);
    }

    @PutMapping("/user")
    @Operation(summary = "更新用户",description = "更新用户")
    public ApiResponse<Integer> updateUser(@RequestBody List<UserDto> userDto) {
        int result = schoolAdminService.updateUser(userDto);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/user")
    @Operation(summary = "删除用户",description = "删除用户")
    public ApiResponse<Integer> deleteUser(@RequestBody List<UserDto> userDto) {
        int result = schoolAdminService.deleteUser(userDto);
        return ApiResponse.ok(result);
    }

    @GetMapping("/user/{id}")
    @Operation(summary = "通过ID获取用户",description = "通过ID获取用户")
    public ApiResponse<User> getUser(@PathVariable String id) {
        User user = schoolAdminService.getUser(id);
        return ApiResponse.ok(user);
    }

    @GetMapping("/user")
    @Operation(summary = "通过条件获取符合条件的用户",description = "比如要搜索姓名含有陈，且手机号码含有133，邮箱含有qaq在相应字段填写即可")
    public ApiResponse<List<User>> getUser(@RequestBody UserDto userDto) {
        List<User> user = schoolAdminService.getUser(userDto);
        return ApiResponse.ok(user);
    }
}
