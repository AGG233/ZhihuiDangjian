package com.rauio.smartdangjian.server.user.controller.user;

import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_NUM_MIN;
import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_SIZE_MAX;
import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_SIZE_MIN;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.server.user.pojo.request.UserUpdateRequest;
import com.rauio.smartdangjian.server.user.pojo.response.UserPublicResponse;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户管理接口", description = "提供用户信息操作，搜索仅返回基本公开信息，不包含邮箱、手机等敏感数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/users")
@SaCheckRole(RoleConstants.STUDENT)
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户的完整信息，基于安全上下文自动识别用户身份，无需传参")
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser() {
        return Result.ok(userService.get(Long.parseLong(userService.getCurrentUserId())));
    }

    @Operation(summary = "获取用户信息", description = "通过ID获取用户信息，返回含脱敏联系方式的用户详情")
    @GetMapping("/{id}")
    public Result<UserResponse> get(@Parameter(name = "id", description = "用户ID") @PathVariable Long id) {
        return Result.ok(userService.get(id));
    }

    @Operation(summary = "用户分页搜索", description = "按条件分页查询用户，仅返回基本公开信息（用户名、姓名、党员信息等），不包含邮箱、手机等敏感数据")
    @PostMapping("/search")
    public Result<Page<UserPublicResponse>> getPage(
            @RequestBody UserRequest userDto,
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") @Min(PAGE_NUM_MIN)
                    int pageNum,
            @Parameter(name = "pageSize", description = "页大小")
                    @RequestParam(defaultValue = "10")
                    @Min(PAGE_SIZE_MIN)
                    @Max(PAGE_SIZE_MAX)
                    int pageSize) {
        return Result.ok(userService.getPage(userDto, pageNum, pageSize));
    }

    @Operation(summary = "更新用户信息", description = "通过ID更新用户信息")
    @PutMapping("/{id}")
    @SaCheckPermission("user:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
        userService.update(id, request);
        return Result.ok(null);
    }

    @Operation(summary = "删除用户（已经弃用）", description = "通过ID删除用户")
    @DeleteMapping("/{id}")
    public Result<Object> delete(@Parameter(name = "id", description = "用户ID") @PathVariable Long id) {
        return Result.ok("404", "接口已经弃用", null);
    }
}
