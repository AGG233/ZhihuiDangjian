package com.rauio.smartdangjian.server.user.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员用户接口", description = "提供管理员侧用户管理能力")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PermissionAccess(UserType.SCHOOL)
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详情")
    @GetMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.USER_MANAGEMENT, action = DataScopeAction.READ, id = "#id")
    public Result<UserVO> get(@Parameter(description = "用户ID") @PathVariable String id) {
        return Result.ok(userService.get(id));
    }

    @Operation(summary = "分页搜索用户", description = "按条件分页查询用户")
    @PostMapping("/search")
    @DataScopeAccess(resource = DataScopeResources.USER_MANAGEMENT, action = DataScopeAction.SEARCH, query = "#userDto")
    public Result<Page<User>> getPage(
            @RequestBody UserDto userDto,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.ok(userService.getPage(userDto, pageNum, pageSize));
    }

    @Operation(summary = "创建用户", description = "由管理员创建用户")
    @PostMapping
    @DataScopeAccess(resource = DataScopeResources.USER_MANAGEMENT, action = DataScopeAction.CREATE, body = "#user")
    public Result<Boolean> create(@RequestBody User user) {
        return Result.ok(userService.register(user));
    }

    @Operation(summary = "更新用户", description = "根据用户ID更新用户")
    @PutMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.USER_MANAGEMENT, action = DataScopeAction.UPDATE, id = "#id", body = "#user")
    public Result<Boolean> update(@PathVariable String id, @RequestBody User user) {
        return Result.ok(userService.update(id, user));
    }
}
