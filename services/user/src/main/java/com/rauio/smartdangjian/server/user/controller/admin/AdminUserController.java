package com.rauio.smartdangjian.server.user.controller.admin;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
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
@SaCheckRole("SCHOOL")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详情，返回完整用户信息")
    @GetMapping("/{id}")
    public Result<User> get(@Parameter(name = "id", description = "用户ID") @PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @Operation(summary = "管理员分页搜索用户", description = "按条件分页查询用户，返回包含邮箱和手机号的完整用户信息，仅供管理员使用")
    @PostMapping("/search")
    public Result<Page<User>> getPage(
            @RequestBody UserRequest userDto,
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "pageSize", description = "页大小") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(userService.getAdminPage(userDto, pageNum, pageSize));
    }

    @Operation(summary = "创建用户", description = "由管理员创建用户")
    @PostMapping
    public Result<Void> create(@RequestBody User user) {
        userService.register(user);
        return Result.ok(null);
    }

    @Operation(summary = "更新用户", description = "根据用户ID更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody User user) {
        userService.update(id, user);
        return Result.ok(null);
    }
}
