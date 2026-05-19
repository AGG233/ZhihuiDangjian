package com.rauio.smartdangjian.server.auth.controller;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.auth.pojo.Captcha;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.pojo.response.LoginResponse;
import com.rauio.smartdangjian.server.auth.service.AuthService;
import com.rauio.smartdangjian.server.auth.service.CaptchaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private AuthController authController;

    // ================================================================
    // getCaptcha
    // ================================================================

    @Test
    @DisplayName("getCaptcha 返回 Captcha 对象包装在 Result 中")
    void getCaptchaReturnsCaptchaInResult() {
        Captcha captcha = Captcha.builder()
                .uuid("uuid-1")
                .base64("base64data")
                .build();
        when(captchaService.get()).thenReturn(captcha);

        Result<Captcha> result = authController.getCaptcha();

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo(captcha);
        assertThat(result.getData().getUuid()).isEqualTo("uuid-1");
    }

    // ================================================================
    // isValid (POST /captcha)
    // ================================================================

    @Test
    @DisplayName("isValid 验证码校验成功返回 true")
    void isValidReturnsTrueWhenCaptchaValid() {
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);

        Result<Boolean> result = authController.isValid("uuid-1", "1234");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isTrue();
    }

    @Test
    @DisplayName("isValid 验证码校验失败返回 false")
    void isValidReturnsFalseWhenCaptchaInvalid() {
        when(captchaService.validate("uuid-1", "WRONG")).thenReturn(false);

        Result<Boolean> result = authController.isValid("uuid-1", "WRONG");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isFalse();
    }

    // ================================================================
    // login
    // ================================================================

    @Test
    @DisplayName("login 委托 authService.login 并返回 LoginResponse")
    void loginDelegatesToAuthService() {
        LoginRequest request = new LoginRequest();
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("jwt-token-xyz")
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        Result<LoginResponse> result = authController.login(request);

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getAccessToken()).isEqualTo("jwt-token-xyz");
        verify(authService).login(request);
    }

    // ================================================================
    // register
    // ================================================================

    @Test
    @DisplayName("register 委托 authService.register 并返回结果")
    void registerDelegatesToAuthService() {
        RegisterRequest request = new RegisterRequest();
        Result<Object> serviceResult = Result.ok("注册成功！");
        when(authService.register(any(RegisterRequest.class))).thenReturn(serviceResult);

        Result<Object> result = authController.register(request);

        assertThat(result).isEqualTo(serviceResult);
        verify(authService).register(request);
    }

    // ================================================================
    // changePassword
    // ================================================================

    @Test
    @DisplayName("changePassword 委托 authService.changePassword 并包装结果为 Result")
    void changePasswordDelegatesToAuthService() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        when(authService.changePassword(any(ChangePasswordRequest.class))).thenReturn(true);

        Result<Boolean> result = authController.changePassword(request);

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isTrue();
        verify(authService).changePassword(request);
    }

    @Test
    @DisplayName("changePassword 修改失败时返回 false")
    void changePasswordReturnsFalseWhenServiceReturnsFalse() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        when(authService.changePassword(any(ChangePasswordRequest.class))).thenReturn(false);

        Result<Boolean> result = authController.changePassword(request);

        assertThat(result.getData()).isFalse();
    }

    // ================================================================
    // logout
    // ================================================================

    @Test
    @DisplayName("logout 委托 authService.logout 并返回空字符串")
    void logoutDelegatesToAuthService() {
        Result<?> result = authController.logout("some-token");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo("");
        verify(authService).logout("some-token");
    }
}
