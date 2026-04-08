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

@Tag(name = "管理员用户接口", description = "提供管理员侧用户管理能力，可查看包含脱敏联系方式在内的完整用户信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PermissionAccess(UserType.SCHOOL)
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详情，返回含脱敏联系方式的完整信息")
    @GetMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.USER_MANAGEMENT, action = DataScopeAction.READ, id = "#id")
    public Result<UserVO> get(@Parameter(name = "id", description = "用户ID") @PathVariable String id) {
        return Result.ok(userService.get(id));
    }

    @Operation(summary = "管理员分页搜索用户", description = "按条件分页查询用户，返回包含脱敏邮箱和手机号的完整用户信息，仅供管理员使用")
    @PostMapping("/search")
    @DataScopeAccess(resource = DataScopeResources.USER_MANAGEMENT, action = DataScopeAction.SEARCH, query = "#userDto")
    public Result<Page<UserVO>> getPage(
            @RequestBody UserDto userDto,
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "pageSize", description = "页大小") @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.ok(userService.getAdminPage(userDto, pageNum, pageSize));
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
