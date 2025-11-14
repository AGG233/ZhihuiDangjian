package com.rauio.ZhihuiDangjian.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.Captcha;
import com.rauio.ZhihuiDangjian.pojo.request.LoginRequest;
import com.rauio.ZhihuiDangjian.pojo.request.RegisterRequest;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.response.LoginResponse;
import com.rauio.ZhihuiDangjian.service.AuthService;
import com.rauio.ZhihuiDangjian.service.CaptchaService;
import com.rauio.ZhihuiDangjian.service.UserService;
import com.rauio.ZhihuiDangjian.utils.CosUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name="认证接口", description = "提供人机验证和登录注册验证操作")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService   authService;
    private final CaptchaService captchaService;
    private final UserService   userService;
    private final ObjectMapper  objectMapper;
    private final CosUtils      cosUtils;

    @Value("${tencent.cloud.cos.secret-key}")
    private String secretKey;

    @Operation(
            summary = "行为验证码图片",
            description = "uuid为验证码的唯一标识码，base64为图片的base64形式，需要转换为图片"
    )
    @GetMapping("/captcha")
    public ResponseEntity<String> getCaptcha() throws JsonProcessingException {
        Captcha captcha = captchaService.get();
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(captcha)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(
            summary = "行为验证码验证接口",
            description = "uuid为验证码的唯一标识码，code为用户输入的验证码，验证成功返回true，验证失败返回false"
    )
    @PostMapping("/captcha")
    public ResponseEntity<String> isValid(@RequestParam String uuid, @RequestParam String code) throws JsonProcessingException {
        Boolean result = captchaService.validate(uuid,code);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .data(result)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(
            summary = "用户注册接口",
            description = "username为用户昵称，password为密码，type为用户类型，" +
                    "type是admin则创建的用户为管理员，teacher则创建的是老师，如果是学生请留空不填"
    )
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) throws JsonProcessingException {
        ApiResponse response = authService.register(request);
        String json = objectMapper.writeValueAsString(response);
        return ResponseEntity.ok(json);
    }

    @Operation(
            summary = "用户登录",
            description = "passport为用户名、手机号码、邮箱或者是身份证号码，登录成功后将返回jwt令牌和用户"
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) throws JsonProcessingException {
        LoginResponse loginResponse = authService.login(request);
        String json = objectMapper.writeValueAsString(ApiResponse.builder()
                .code("200")
                .message("登录成功")
                .data(loginResponse)
                .build());
        return ResponseEntity.ok(json);
    }

    @Operation(
            summary = "用户登出",
            description = "登出成功后将返回一个空的响应体"
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String refreshToken) throws JsonProcessingException {
        ApiResponse response = authService.logout(refreshToken);
        String json = objectMapper.writeValueAsString(response);
        return ResponseEntity.ok(json);
    }

    @Operation(
            summary = "用户刷新令牌",
            description = "刷新令牌成功后将返回新的jwt令牌"
    )
    @GetMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestParam String refreshToken, @RequestParam String accessToken) throws JsonProcessingException {
        ApiResponse response = authService.refresh(refreshToken, accessToken);
        String json = objectMapper.writeValueAsString(response);
        return ResponseEntity.ok(json);
    }

    /*
    * 腾讯云COS临时密钥获取
    * */
//    @GetMapping("/resKey")
//    public ApiResponse getResKey(){
//        Credentials  credentials = cosUtils.testGetCredential(secretKey).credentials;
//        return ApiResponse.builder()
//                .data(CosStsDto.builder()
//                        .sessionToken(credentials.sessionToken)
//                        .tmpSecretKey(credentials.tmpSecretKey)
//                        .tmpSecretId(credentials.tmpSecretId)
//                        .token(credentials.token)
//                        .build()
//                ).build();
//    }
}