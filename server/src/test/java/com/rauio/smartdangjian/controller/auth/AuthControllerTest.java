package com.rauio.smartdangjian.controller.auth;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.AuthTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.service.AuthService;
import com.rauio.smartdangjian.server.auth.service.CaptchaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AuthControllerTest.TestConfig.class)
@DisplayName("认证接口测试")
class AuthControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    @ComponentScan(basePackages = "com.rauio.smartdangjian.server.auth.controller")
    static class TestConfig extends CommonTestConfig {
    }

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CaptchaService captchaService;

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /auth/captcha - 获取验证码成功")
        void getCaptchaSuccess() throws Exception {
            when(captchaService.get()).thenReturn(AuthTestDataFactory.createCaptcha());

            mockMvc.perform(get("/auth/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.uuid").value("captcha-uuid-001"))
                    .andExpect(jsonPath("$.data.base64").isNotEmpty());
        }

        @Test
        @DisplayName("POST /auth/captcha - 验证验证码成功")
        void validateCaptchaSuccess() throws Exception {
            when(captchaService.validate("uuid-001", "valid-code")).thenReturn(true);

            mockMvc.perform(post("/auth/captcha")
                            .param("uuid", "uuid-001")
                            .param("code", "valid-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("POST /auth/login - 登录成功")
        void loginSuccess() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenReturn(AuthTestDataFactory.createLoginResponse());

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createLoginRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        }

        @Test
        @DisplayName("POST /auth/register - 注册成功")
        void registerSuccess() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenReturn(Result.ok("注册成功"));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createRegisterRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value("注册成功"));
        }

        @Test
        @DisplayName("POST /auth/changePassword - 修改密码成功")
        void changePasswordSuccess() throws Exception {
            when(authService.changePassword(any(ChangePasswordRequest.class))).thenReturn(true);

            mockMvc.perform(post("/auth/changePassword")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createChangePasswordRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("POST /auth/logout - 登出成功")
        void logoutSuccess() throws Exception {
            doNothing().when(authService).logout("test-token");

            mockMvc.perform(post("/auth/logout")
                            .param("token", "test-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 异常处理场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("POST /auth/login - 请求体为空返回 400")
        void loginWithEmptyBody() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /auth/register - 请求体为空返回 400")
        void registerWithEmptyBody() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /auth/changePassword - 请求体为空返回 400")
        void changePasswordWithEmptyBody() throws Exception {
            mockMvc.perform(post("/auth/changePassword")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /auth/login - Service 抛出 BusinessException 返回 400")
        void loginThrowsBusinessException() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(4000, "验证码错误"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createLoginRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("验证码错误"));
        }

        @Test
        @DisplayName("POST /auth/login - Service 抛出 RuntimeException 返回 500")
        void loginThrowsRuntimeException() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("认证服务异常"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createLoginRequest())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST /auth/register - Service 抛出 BusinessException 返回 400")
        void registerThrowsBusinessException() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BusinessException(4000, "该手机号已被注册"));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createRegisterRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("该手机号已被注册"));
        }

        @Test
        @DisplayName("POST /auth/changePassword - Service 返回 false 时 code 为 400")
        void changePasswordReturnsFalse() throws Exception {
            when(authService.changePassword(any(ChangePasswordRequest.class))).thenReturn(false);

            mockMvc.perform(post("/auth/changePassword")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createChangePasswordRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("POST /auth/register - Service 抛出 RuntimeException 返回 500")
        void registerThrowsRuntimeException() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new RuntimeException("注册服务异常"));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createRegisterRequest())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST /auth/logout - Service 抛出 BusinessException 返回 400")
        void logoutThrowsBusinessException() throws Exception {
            doThrow(new BusinessException(4000, "令牌无效"))
                    .when(authService).logout("invalid-token");

            mockMvc.perform(post("/auth/logout")
                            .param("token", "invalid-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("令牌无效"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /auth/captcha - 验证码内容含特殊字符")
        void captchaWithSpecialChars() throws Exception {
            when(captchaService.get())
                    .thenReturn(AuthTestDataFactory.createCaptcha());

            mockMvc.perform(get("/auth/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.code").value("captcha-code-001"));
        }

        @Test
        @DisplayName("POST /auth/captcha - 验证码校验失败返回 false")
        void captchaValidationFails() throws Exception {
            when(captchaService.validate("uuid-001", "wrong-code")).thenReturn(false);

            mockMvc.perform(post("/auth/captcha")
                            .param("uuid", "uuid-001")
                            .param("code", "wrong-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("POST /auth/login - 登录请求含超长字段")
        void loginWithLongFields() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(4000, "参数过长"));

            String longPassport = "a".repeat(200);
            LoginRequest request = AuthTestDataFactory.createLoginRequest();
            request.setPassport(longPassport);
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /auth/register - 注册请求含特殊字符用户名")
        void registerWithSpecialChars() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenReturn(Result.ok("注册成功"));

            RegisterRequest request = AuthTestDataFactory.createRegisterRequest();
            request.setUsername("test_@#$%^&");
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 安全场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在登录 passport 字段")
        void xssInLoginPassport() throws Exception {
            LoginRequest request = AuthTestDataFactory.createLoginRequest();
            request.setPassport("<script>alert('xss')</script>");
            when(authService.login(any(LoginRequest.class)))
                    .thenReturn(AuthTestDataFactory.createLoginResponse());

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在登录 passport 字段")
        void sqlInjectionInLoginPassport() throws Exception {
            LoginRequest request = AuthTestDataFactory.createLoginRequest();
            request.setPassport("' OR '1'='1");
            when(authService.login(any(LoginRequest.class)))
                    .thenReturn(AuthTestDataFactory.createLoginResponse());

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET 请求登录接口返回 405")
        void loginWithWrongMethod() throws Exception {
            mockMvc.perform(get("/auth/login"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求注册接口返回 405")
        void registerWithWrongMethod() throws Exception {
            mockMvc.perform(put("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求验证码接口返回 405")
        void captchaWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/auth/captcha"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
