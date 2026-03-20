package com.rauio.smartdangjian.controller.admin.school;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.pojo.dto.UserDto;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学校管理员用户控制器，提供限定在当前学校范围内的用户管理接口。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
@Tag(name = "学校管理员用户接口")
public class SchoolAdminUserController {

    private final AdminUserManagementService adminUserManagementService;

    /**
     * 为当前学校批量新增用户。
     *
     * @param userDtoList 用户请求体列表
     * @return 是否新增成功
     */
    @PostMapping("/school/user")
    @Operation(summary = "添加用户", description = "添加用户")
    public Result<Boolean> addSchoolUser(@RequestBody @Valid List<UserDto> userDtoList) {
        Boolean result = adminUserManagementService.addUsers(userDtoList);
        return Result.ok(result);
    }

    /**
     * 在当前学校范围内批量更新用户。
     *
     * @param userDto 用户请求体列表
     * @return 是否更新成功
     */
    @PutMapping("/school/user")
    @Operation(summary = "更新用户", description = "更新用户")
    public Result<Boolean> updateSchoolUser(@RequestBody @Valid List<UserDto> userDto) {
        Boolean result = adminUserManagementService.updateUsers(userDto);
        return Result.ok(result);
    }

    /**
     * 在当前学校范围内批量删除用户。
     *
     * @param userIdList 用户 ID 列表
     * @return 是否删除成功
     */
    @DeleteMapping("/school/user")
    @Operation(summary = "删除用户", description = "删除用户")
    public Result<Boolean> deleteSchoolUser(@RequestBody @Valid List<String> userIdList) {
        Boolean result = adminUserManagementService.deleteUsers(userIdList);
        return Result.ok(result);
    }

    /**
     * 获取当前学校范围内指定用户的详细信息。
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/school/user/{id}")
    @Operation(summary = "通过ID获取用户", description = "通过ID获取用户")
    public Result<User> getSchoolUser(@PathVariable String id) {
        User user = adminUserManagementService.getUser(id);
        return Result.ok(user);
    }

    /**
     * 分页查询当前学校范围内符合条件的用户。
     *
     * @param userDto 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户分页结果
     */
    @GetMapping("/school/users")
    @Operation(summary = "通过条件获取符合条件的用户", description = "比如要搜索姓名含有陈，且手机号码含有133，邮箱含有qaq的用户，在姓名、手机号码，邮箱字段填写陈、133、qaq即可")
    public Result<Page<User>> getSchoolUser(
            @ModelAttribute @Valid UserDto userDto,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页的大小") @RequestParam(defaultValue = "10") int pageSize) {
        Page<User> user = adminUserManagementService.getUsers(userDto, pageNum, pageSize);
        return Result.ok(user);
    }
}
