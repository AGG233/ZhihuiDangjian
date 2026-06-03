package com.rauio.smartdangjian.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.ControllerTestConfiguration;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.controller.factory.AuthTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.service.AuthService;
import com.rauio.smartdangjian.server.auth.service.CaptchaService;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ControllerTestConfiguration.class)
@DisplayName("认证接口测试")
class AuthControllerTest extends BaseControllerTest {

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

            mockMvc.perform(get("/api/auth/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.uuid").value("captcha-uuid-001"))
                    .andExpect(jsonPath("$.data.base64").isNotEmpty());
        }

        @Test
        @DisplayName("POST /auth/captcha - 验证验证码成功")
        void validateCaptchaSuccess() throws Exception {
            when(captchaService.validate("uuid-001", "valid-code")).thenReturn(true);

            mockMvc.perform(post("/api/auth/captcha").param("uuid", "uuid-001").param("code", "valid-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("POST /auth/login - 登录成功")
        void loginSuccess() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenReturn(AuthTestDataFactory.createLoginResponse());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createLoginRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        }

        @Test
        @DisplayName("POST /auth/register - 注册成功")
        void registerSuccess() throws Exception {
            when(authService.register(any(RegisterRequest.class))).thenReturn(Result.ok("注册成功"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createRegisterRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value("注册成功"));
        }

        @Test
        @DisplayName("POST /auth/changePassword - 修改密码成功")
        void changePasswordSuccess() throws Exception {
            doNothing().when(authService).changePassword(any(ChangePasswordRequest.class));

            mockMvc.perform(post("/api/auth/changePassword")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createChangePasswordRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST /auth/logout - 登出成功")
        void logoutSuccess() throws Exception {
            doNothing().when(authService).logout();

            mockMvc.perform(post("/api/auth/logout"))
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
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /auth/register - 请求体为空返回 400")
        void registerWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /auth/changePassword - 请求体为空返回 400")
        void changePasswordWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/auth/changePassword")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /auth/login - Service 抛出 BusinessException 返回 400")
        void loginThrowsBusinessException() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(AuthErrorConstants.CAPTCHA_ERROR, "验证码错误"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createLoginRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("1010"))
                    .andExpect(jsonPath("$.message").value("验证码错误"));
        }

        @Test
        @DisplayName("POST /auth/login - Service 抛出 RuntimeException 返回 500")
        void loginThrowsRuntimeException() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("认证服务异常"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createLoginRequest())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST /auth/register - Service 抛出 BusinessException 返回 400")
        void registerThrowsBusinessException() throws Exception {
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BusinessException(UserErrorConstants.PHONE_EXISTS, "该手机号已被注册"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createRegisterRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("2001"))
                    .andExpect(jsonPath("$.message").value("该手机号已被注册"));
        }

        @Test
        @DisplayName("POST /auth/changePassword - Service 抛出 BusinessException 时返回 400")
        void changePasswordThrowsBusinessException() throws Exception {
            doThrow(new BusinessException(AuthErrorConstants.OLD_PASSWORD_ERROR, "旧密码错误"))
                    .when(authService)
                    .changePassword(any(ChangePasswordRequest.class));

            mockMvc.perform(post("/api/auth/changePassword")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createChangePasswordRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("1014"))
                    .andExpect(jsonPath("$.message").value("旧密码错误"));
        }

        @Test
        @DisplayName("POST /auth/register - Service 抛出 RuntimeException 返回 500")
        void registerThrowsRuntimeException() throws Exception {
            when(authService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("注册服务异常"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(AuthTestDataFactory.createRegisterRequest())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST /auth/logout - Service 抛出 BusinessException 返回 400")
        void logoutThrowsBusinessException() throws Exception {
            doThrow(new BusinessException(AuthErrorConstants.TOKEN_VERIFICATION_FAILED, "令牌无效"))
                    .when(authService)
                    .logout();

            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("1004"))
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
            when(captchaService.get()).thenReturn(AuthTestDataFactory.createCaptcha());

            mockMvc.perform(get("/api/auth/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.code").value("captcha-code-001"));
        }

        @Test
        @DisplayName("POST /auth/captcha - 验证码校验失败返回 false")
        void captchaValidationFails() throws Exception {
            when(captchaService.validate("uuid-001", "wrong-code")).thenReturn(false);

            mockMvc.perform(post("/api/auth/captcha").param("uuid", "uuid-001").param("code", "wrong-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("POST /auth/login - 登录请求含超长字段")
        void loginWithLongFields() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(ErrorConstants.ARGS_ERROR, "参数过长"));

            String longPassport = "a".repeat(200);
            LoginRequest request = AuthTestDataFactory.createLoginRequest();
            request.setPassport(longPassport);
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /auth/register - 注册请求含特殊字符用户名")
        void registerWithSpecialChars() throws Exception {
            when(authService.register(any(RegisterRequest.class))).thenReturn(Result.ok("注册成功"));

            RegisterRequest request = AuthTestDataFactory.createRegisterRequest();
            request.setUsername("test_@#$%^&");
            mockMvc.perform(post("/api/auth/register")
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
            when(authService.login(any(LoginRequest.class))).thenReturn(AuthTestDataFactory.createLoginResponse());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在登录 passport 字段")
        void sqlInjectionInLoginPassport() throws Exception {
            LoginRequest request = AuthTestDataFactory.createLoginRequest();
            request.setPassport("' OR '1'='1");
            when(authService.login(any(LoginRequest.class))).thenReturn(AuthTestDataFactory.createLoginResponse());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET 请求登录接口返回 405")
        void loginWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/auth/login")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求注册接口返回 405")
        void registerWithWrongMethod() throws Exception {
            mockMvc.perform(put("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求验证码接口返回 405")
        void captchaWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/auth/captcha")).andExpect(status().isMethodNotAllowed());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 参数校验场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("参数校验场景")
    class ValidationTests {

        private RegisterRequest createValidRequest() {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("testuser");
            req.setPassword("Test1234!");
            req.setRealName("张三");
            req.setIdCard("110101199001011234");
            req.setPhone("13800138000");
            req.setCaptchaUUID("uuid");
            req.setCaptchaCode("8888");
            req.setUniversityId("uni-1");
            req.setPartyStatus(PartyStatus.FORMAL_MEMBER);
            return req;
        }

        @ParameterizedTest
        @MethodSource("invalidUsernameProvider")
        @DisplayName("用户名非法参数返回400")
        void invalidUsername(String username) throws Exception {
            RegisterRequest req = createValidRequest();
            req.setUsername(username);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidUsernameProvider() {
            return Stream.of(
                    Arguments.of((Object) null), Arguments.of(""), Arguments.of("a"), Arguments.of("a".repeat(17)));
        }

        @ParameterizedTest
        @MethodSource("invalidPasswordProvider")
        @DisplayName("密码非法参数返回400")
        void invalidPassword(String password) throws Exception {
            RegisterRequest req = createValidRequest();
            req.setPassword(password);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidPasswordProvider() {
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(""),
                    Arguments.of("short1"),
                    Arguments.of("noupper123!"),
                    Arguments.of("NoSpecial123"),
                    Arguments.of("a".repeat(21)));
        }

        @ParameterizedTest
        @MethodSource("invalidRealNameProvider")
        @DisplayName("真实姓名非法参数返回400")
        void invalidRealName(String realName) throws Exception {
            RegisterRequest req = createValidRequest();
            req.setRealName(realName);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidRealNameProvider() {
            return Stream.of(
                    Arguments.of((Object) null), Arguments.of(""), Arguments.of("a"), Arguments.of("a".repeat(17)));
        }

        @ParameterizedTest
        @MethodSource("invalidIdCardProvider")
        @DisplayName("身份证号非法参数返回400")
        void invalidIdCard(String idCard) throws Exception {
            RegisterRequest req = createValidRequest();
            req.setIdCard(idCard);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidIdCardProvider() {
            return Stream.of(
                    Arguments.of((Object) null),
                    Arguments.of(""),
                    Arguments.of("123"),
                    Arguments.of("1234567890123456"));
        }

        @ParameterizedTest
        @MethodSource("invalidPhoneProvider")
        @DisplayName("手机号非法参数返回400")
        void invalidPhone(String phone) throws Exception {
            RegisterRequest req = createValidRequest();
            req.setPhone(phone);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidPhoneProvider() {
            return Stream.of(
                    Arguments.of((Object) null), Arguments.of(""), Arguments.of("123"), Arguments.of("12345678901"));
        }

        @ParameterizedTest
        @MethodSource("invalidEmailProvider")
        @DisplayName("邮箱非法参数返回400")
        void invalidEmail(String email) throws Exception {
            RegisterRequest req = createValidRequest();
            req.setEmail(email);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidEmailProvider() {
            return Stream.of(Arguments.of("not-email"), Arguments.of("@example.com"), Arguments.of("user@"));
        }

        @ParameterizedTest
        @MethodSource("invalidPartyMemberIdProvider")
        @DisplayName("党员编号非法参数返回400")
        void invalidPartyMemberId(String partyMemberId) throws Exception {
            RegisterRequest req = createValidRequest();
            req.setPartyMemberId(partyMemberId);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AuthTestDataFactory.toJson(req)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidPartyMemberIdProvider() {
            return Stream.of(Arguments.of("a".repeat(19)), Arguments.of("a".repeat(21)));
        }
    }
}
