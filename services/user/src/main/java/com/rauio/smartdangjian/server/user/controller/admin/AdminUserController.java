package com.rauio.smartdangjian.server.user.controller.admin;

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
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员用户接口", description = "提供管理员侧用户管理能力，可查看包含联系方式在内的完整用户信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@SaCheckRole(RoleConstants.SCHOOL)
@Validated
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详情，返回完整用户信息")
    @GetMapping("/{id}")
    public Result<UserResponse> get(@Parameter(name = "id", description = "用户ID") @PathVariable Long id) {
        return Result.ok(userService.get(id));
    }

    @Operation(summary = "管理员分页搜索用户", description = "按条件分页查询用户，返回包含邮箱和手机号的完整用户信息，仅供管理员使用")
    @PostMapping("/search")
    public Result<Page<UserResponse>> getPage(
            @RequestBody UserRequest userDto,
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") @Min(PAGE_NUM_MIN)
                    int pageNum,
            @Parameter(name = "pageSize", description = "页大小")
                    @RequestParam(defaultValue = "10")
                    @Min(PAGE_SIZE_MIN)
                    @Max(PAGE_SIZE_MAX)
                    int pageSize) {
        return Result.ok(userService.getAdminResponsePage(userDto, pageNum, pageSize));
    }

    @Operation(summary = "创建用户", description = "由管理员创建用户")
    @PostMapping
    public Result<Void> create(@RequestBody @Valid UserRequest request) {
        userService.register(request);
        return Result.ok(null);
    }

    @Operation(summary = "更新用户", description = "根据用户ID更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid UserRequest request) {
        userService.update(id, request);
        return Result.ok(null);
    }

    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(name = "id", description = "用户ID") @PathVariable Long id) {
        userService.delete(id);
        return Result.ok(null);
    }
}
