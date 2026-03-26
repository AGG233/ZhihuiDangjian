package com.rauio.smartdangjian.server.user.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.server.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理接口", description = "提供用户信息操作")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户信息", description = "通过ID获取用户信息")
    @GetMapping("/{id}")
    public Result<UserVO> get(@Parameter(description = "用户ID") @PathVariable String id) {
        return Result.ok(userService.get(id));
    }

    @Operation(summary = "分页查询用户", description = "通过用户请求体的信息模糊查询条件匹配的用户")
    @PostMapping("/search")
    public Result<Page<User>> getPage(
            @RequestBody UserDto userDto,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.ok(userService.getPage(userDto, pageNum, pageSize));
    }

    @Operation(summary = "更新用户信息", description = "通过ID更新用户信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable String id, @RequestBody User user) {
        return Result.ok(userService.update(id, user));
    }

    @Operation(summary = "删除用户（已经弃用）", description = "通过ID删除用户")
    @DeleteMapping("/{id}")
    public Result<Object> delete(@Parameter(description = "用户ID") @PathVariable String id) {
        userService.delete(id);
        return Result.ok("404", "接口已经弃用", null);
    }
}
