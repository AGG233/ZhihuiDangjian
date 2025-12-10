package com.rauio.ZhihuiDangjian.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.Captcha;
import com.rauio.ZhihuiDangjian.pojo.request.LoginRequest;
import com.rauio.ZhihuiDangjian.pojo.request.RegisterRequest;
import com.rauio.ZhihuiDangjian.pojo.response.LoginResponse;
import com.rauio.ZhihuiDangjian.pojo.response.Result;
import com.rauio.ZhihuiDangjian.service.AuthService;
import com.rauio.ZhihuiDangjian.service.CaptchaService;
import com.rauio.ZhihuiDangjian.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name="认证接口", description = "提供人机验证和登录注册验证操作")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService   authService;
    private final CaptchaService captchaService;
    private final ObjectMapper  objectMapper;
    private final UserService userService;

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
            summary = "用户注册接口（已经弃用）",
            description = "username为用户昵称，password为密码，type为用户类型，" +
                    "type是admin则创建的用户为管理员，teacher则创建的是老师，如果是学生请留空不填"
    )
    @Deprecated
    @PostMapping("/register")
    public Result register(@RequestBody @Valid RegisterRequest request){
        return authService.register(request);
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
            summary = "用户登出",
            description = "登出成功后将返回一个空的响应体"
    )
    @PostMapping("/logout")
    public Result logout(@RequestParam @Valid String refreshToken){

        return authService.logout(refreshToken);
    }
    @PostMapping("/changePassword")
    public Result<Boolean> changePassword(@RequestParam @Valid String oldPassword, @RequestParam @Valid String newPassword){
        Boolean result = userService.changePassword(oldPassword,newPassword);
        return Result.ok(result);
    }
    @Operation(
            summary = "用户刷新令牌",
            description = "刷新令牌成功后将返回新的jwt令牌"
    )
    @GetMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestParam @Valid String refreshToken){
        return authService.refresh(refreshToken);
    }
}