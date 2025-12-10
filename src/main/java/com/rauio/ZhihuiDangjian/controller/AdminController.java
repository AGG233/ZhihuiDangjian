package com.rauio.ZhihuiDangjian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import com.rauio.ZhihuiDangjian.pojo.response.Result;
import com.rauio.ZhihuiDangjian.service.AdminService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
@Slf4j
@Tag(name = "统一管理员接口")
public class AdminController {

    private final AdminService adminService;

    // 系统管理员接口功能
    
    @GetMapping("/users")
    @Operation(description = "获取用户信息")
    public Result<Page<User>> getUser(
            @Parameter(description = "搜索条件")    @ModelAttribute UserDto userDto,
            @Parameter(description = "页码")      @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页的大小")    @RequestParam(defaultValue = "10") int pageSize) {
        Page<User> result = adminService.getUser(userDto, pageNum, pageSize);
        return Result.ok(result);
    }

    @PutMapping("/user")
    @Operation(description = "更新普通用户和高校管理员信息，请求体为一个列表，列表元素为用户请求体")
    public Result<Boolean> updateUser(@RequestBody List<UserDto> user) {
        Boolean result = adminService.updateUser(user);
        return Result.ok(result);
    }

    @PostMapping("/user")
    @Operation(description = "添加用户，请求体为一个列表，列表元素为用户请求体")
    public Result<Boolean> addUser(@RequestBody List<UserDto> user){
        Boolean result = adminService.addUser(user);
        return Result.ok(result);
    }

    @DeleteMapping("/user")
    @Operation(description = "删除用户，请求体为一个用户ID列表")
    public Result<Boolean> deleteUser(@RequestBody List<String> idList) {
        Boolean result = adminService.deleteUser(idList);
        return Result.ok(result);
    }

    // 学校管理员接口功能
    @PostMapping("/school/user")
    @Operation(summary = "添加用户", description = "添加用户")
    public Result<Boolean> addSchoolUser(@RequestBody List<UserDto> userDtoList) {
        Boolean result = adminService.addSchoolUser(userDtoList);
        return Result.ok(result);
    }

    @PutMapping("/school/user")
    @Operation(summary = "更新用户", description = "更新用户")
    public Result<Boolean> updateSchoolUser(@RequestBody List<UserDto> userDto) {
        Boolean result = adminService.updateSchoolUser(userDto);
        return Result.ok(result);
    }

    @DeleteMapping("/school/user")
    @Operation(summary = "删除用户", description = "删除用户")
    public Result<Boolean> deleteSchoolUser(@RequestBody List<String> userIdList) {
        Boolean result = adminService.deleteSchoolUser(userIdList);
        return Result.ok(result);
    }

    @GetMapping("/school/user/{id}")
    @Operation(summary = "通过ID获取用户", description = "通过ID获取用户")
    public Result<User> getSchoolUser(@PathVariable String id) {
        User user = adminService.getSchoolUser(id);
        return Result.ok(user);
    }

    @GetMapping("/school/users")
    @Operation(summary = "通过条件获取符合条件的用户", description = "比如要搜索姓名含有陈，且手机号码含有133，邮箱含有qaq的用户，在姓名、手机号码，邮箱字段填写陈、133、qaq即可")
    public Result<Page<User>> getSchoolUser(
            @ModelAttribute UserDto userDto,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页的大小") @RequestParam(defaultValue = "10") int pageSize) {
        Page<User> user = adminService.getSchoolUser(userDto, pageNum, pageSize);
        return Result.ok(user);
    }
}