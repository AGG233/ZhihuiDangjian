package com.rauio.smartdangjian.controller.admin.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.service.admin.AdminUserManagementService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统管理员用户控制器，提供通用用户管理接口。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
@Tag(name = "系统管理员用户接口")
public class AdminUserController {

    private final AdminUserManagementService adminUserManagementService;

    /**
     * 分页查询用户信息。
     *
     * @param userDto 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户分页结果
     */
    @GetMapping("/users")
    @Operation(description = "获取用户信息")
    public Result<Page<User>> getUser(
            @Parameter(description = "搜索条件") @ModelAttribute UserDto userDto,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页的大小") @RequestParam(defaultValue = "10") int pageSize) {
        Page<User> result = adminUserManagementService.getPage(userDto, pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 批量更新普通用户或高校管理员信息。
     *
     * @param user 用户请求体列表
     * @return 是否更新成功
     */
    @PutMapping("/user")
    @Operation(description = "更新普通用户和高校管理员信息，请求体为一个列表，列表元素为用户请求体")
    public Result<Boolean> updateUser(@RequestBody @Valid List<UserDto> user) {
        Boolean result = adminUserManagementService.update(user);
        return Result.ok(result);
    }

    /**
     * 批量新增普通用户或高校管理员。
     *
     * @param user 用户请求体列表
     * @return 是否新增成功
     */
    @PostMapping("/user")
    @Operation(description = "添加用户，请求体为一个列表，列表元素为用户请求体")
    public Result<Boolean> addUser(@RequestBody @Valid List<UserDto> user) {
        Boolean result = adminUserManagementService.create(user);
        return Result.ok(result);
    }

    /**
     * 按用户 ID 批量删除用户。
     *
     * @param idList 用户 ID 列表
     * @return 是否删除成功
     */
    @DeleteMapping("/user")
    @Operation(description = "删除用户，请求体为一个用户ID列表")
    public Result<Boolean> deleteUser(@RequestBody @Valid List<String> idList) {
        Boolean result = adminUserManagementService.delete(idList);
        return Result.ok(result);
    }
}
