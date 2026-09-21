package com.rauio.smartdangjian.server.user.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 当前登录用户身份接口。
 *
 * <p>独立于 {@link UserController}：后者带类级 {@code @SaCheckRole("STUDENT")}，
 * 若把 {@code /me} 并入会限制学校/管理员角色获取自身信息。本控制器只要求登录态，
 * 由全局 SaInterceptor 保证；{@code /me} 为字面量路径，优先级高于 UserController
 * 的 {@code /{id}}，不会把 "me" 当作 ID 解析。
 */
@Tag(name = "当前用户接口", description = "获取当前登录用户自身的身份信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/users")
public class UserIdentityController {

    private final UserService userService;

    @Operation(summary = "获取当前登录用户信息", description = "返回当前登录用户自身的身份资料，无需传入用户 ID")
    @GetMapping("/me")
    public Result<UserResponse> me() {
        return Result.ok(userService.getCurrentUserProfile());
    }
}
