package com.rauio.smartdangjian.server.auth.controller;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.auth.pojo.Captcha;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.pojo.response.LoginResponse;
import com.rauio.smartdangjian.server.auth.service.AuthService;
import com.rauio.smartdangjian.server.auth.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name="认证接口", description = "提供人机验证和登录注册验证操作")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService   authService;
    private final CaptchaService captchaService;

    @Operation(
            summary = "行为验证码图片",
            description = "uuid为验证码的唯一标识码，base64为图片的base64形式，需要转换为图片"
    )
    @GetMapping("/captcha")
    public Result<Captcha> getCaptcha(){
        Captcha captcha = captchaService.get();
        return Result.ok(captcha);
    }

    @Operation(
            summary = "行为验证码验证接口",
            description = "uuid为验证码的唯一标识码，code为用户输入的验证码，验证成功返回true，验证失败返回false"
    )
    @PostMapping("/captcha")
    public Result<Boolean> isValid(@RequestParam @Valid  String uuid, @RequestParam @Valid String code){
        Boolean result = captchaService.validate(uuid,code);
        return Result.ok(result);
    }

    @Operation(
            summary = "统一用户登录",
            description = "需要人机验证（获取验证码以及校验验证码是否有误），登录成功后将返回jwt令牌，大部分接口访问都需要在请求头的Authorization字段添加如下格式：Bearer <替换为jwt令牌>"
    )
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request){
        LoginResponse loginResponse = authService.login(request);
        return Result.ok(loginResponse);
    }

    @Operation(
            summary = "用户注册",
            description = "注册新用户账户"
    )
    @PostMapping("/register")
    public Result<Object> register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(
            summary = "修改密码",
            description = "已登录用户修改自己的密码"
    )
    @PostMapping("/changePassword")
    public Result<Boolean> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return Result.ok(authService.changePassword(request));
    }

    @Operation(
            summary = "用户登出",
            description = "登出成功后将返回一个空的响应体"
    )
    @PostMapping("/logout")
    public Result logout(@RequestParam @Valid String token){
        authService.logout(token);
        return Result.ok("");
    }
}
