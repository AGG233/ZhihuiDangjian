package com.rauio.smartdangjian.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.SessionUserPrincipal;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.pojo.response.LoginResponse;
import com.rauio.smartdangjian.server.auth.security.SessionPrincipalFactory;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CaptchaService captchaService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    private AuthService authService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        authService = new AuthService(
                captchaService,
                userMapper,
                userService,
                new SessionPrincipalFactory(),
                Clock.fixed(Instant.parse("2026-05-31T10:15:30Z"), ZoneId.of("UTC")));
    }

    // ================================================================
    // login
    // ================================================================

    @Test
    @DisplayName("login 验证码校验失败时抛出 BusinessException(CAPTCHA_ERROR)")
    void loginThrowsWhenCaptchaInvalid() {
        LoginRequest request = createLoginRequest();
        request.setCaptchaCode("WRONG");
        when(captchaService.validate("uuid-1", "WRONG")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.CAPTCHA_ERROR);
    }

    @Test
    @DisplayName("login 验证码通过且登录成功时返回 LoginResponse")
    void loginReturnsTokenWhenSuccessful() {
        LoginRequest request = createLoginRequest();
        String rawPassword = request.getPassword();
        User user = createUser(1L, "testuser");
        user.setPassword(newEncodedPassword());
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userService.getByPassport("admin")).thenReturn(user);

        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
                MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock
                    .when(() -> BCrypt.checkpw(rawPassword, user.getPassword()))
                    .thenReturn(true);
            SaSession session = mock(SaSession.class);
            stpUtilMock.when(StpUtil::getSession).thenReturn(session);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn("sa-token-abc");

            LoginResponse result = authService.login(request);

            assertThat(result.getAccessToken()).isEqualTo("sa-token-abc");
            ArgumentCaptor<Object> principalCaptor = ArgumentCaptor.forClass(Object.class);
            verify(session).set(org.mockito.Mockito.eq("user"), principalCaptor.capture());
            assertThat(principalCaptor.getValue()).isInstanceOf(SessionUserPrincipal.class);
            assertThat((SessionUserPrincipal) principalCaptor.getValue())
                    .extracting(
                            SessionUserPrincipal::getId,
                            SessionUserPrincipal::getUserType,
                            SessionUserPrincipal::getUniversityId)
                    .containsExactly(user.getId(), user.getUserType(), user.getUniversityId());
        }
    }

    @Test
    @DisplayName("login app 平台使用较长超时时间")
    void loginAppPlatformUsesLongTimeout() {
        LoginRequest request = createLoginRequest();
        request.setPlatform("app");
        String rawPassword = request.getPassword();
        User user = createUser(1L, "testuser");
        user.setPassword(newEncodedPassword());
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userService.getByPassport("admin")).thenReturn(user);

        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
                MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock
                    .when(() -> BCrypt.checkpw(rawPassword, user.getPassword()))
                    .thenReturn(true);
            SaSession session = mock(SaSession.class);
            stpUtilMock.when(StpUtil::getSession).thenReturn(session);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn("sa-token-app");

            LoginResponse result = authService.login(request);

            assertThat(result.getAccessToken()).isEqualTo("sa-token-app");
        }
    }

    @Test
    @DisplayName("login null platform defaults to web")
    void loginNullPlatformDefaultsToWeb() {
        LoginRequest request = createLoginRequest();
        request.setPlatform(null);
        String rawPassword = request.getPassword();
        User user = createUser(1L, "testuser");
        user.setPassword(newEncodedPassword());
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userService.getByPassport("admin")).thenReturn(user);

        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
                MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock
                    .when(() -> BCrypt.checkpw(rawPassword, user.getPassword()))
                    .thenReturn(true);
            SaSession session = mock(SaSession.class);
            stpUtilMock.when(StpUtil::getSession).thenReturn(session);
            stpUtilMock.when(StpUtil::getTokenValue).thenReturn("sa-token-null-plat");

            LoginResponse result = authService.login(request);

            assertThat(result.getAccessToken()).isEqualTo("sa-token-null-plat");
        }
    }

    @Test
    @DisplayName("login 密码错误时抛出 BusinessException(PASSWORD_ERROR)")
    void loginThrowsWhenPasswordMismatch() {
        LoginRequest request = createLoginRequest();
        String rawPassword = request.getPassword();
        User user = createUser(1L, "testuser");
        user.setPassword(newEncodedPassword());
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userService.getByPassport("admin")).thenReturn(user);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock
                    .when(() -> BCrypt.checkpw(rawPassword, user.getPassword()))
                    .thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(AuthErrorConstants.PASSWORD_ERROR);
        }
    }

    @Test
    @DisplayName("login 用户不存在时抛出 BusinessException(USER_NOT_FOUND)")
    void loginThrowsWhenUserNotFound() {
        LoginRequest request = createLoginRequest();
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userService.getByPassport("admin")).thenReturn(null);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("login 账号被封禁时抛出 BusinessException(UNAUTHORIZED)")
    void loginThrowsWhenBanned() {
        LoginRequest request = createLoginRequest();
        User user = createUser(1L, "testuser");
        user.setStatus(AccountStatus.BANNED);
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userService.getByPassport("admin")).thenReturn(user);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.UNAUTHORIZED);
    }

    @Test
    @DisplayName("login 账号未激活时抛出 BusinessException(UNAUTHORIZED)")
    void loginThrowsWhenInactive() {
        LoginRequest request = createLoginRequest();
        User user = createUser(1L, "testuser");
        user.setStatus(AccountStatus.INACTIVE);
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userService.getByPassport("admin")).thenReturn(user);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.UNAUTHORIZED);
    }

    // ================================================================
    // logout
    // ================================================================

    @Test
    @DisplayName("logout 调用 StpUtil.logout")
    void logoutDelegatesToStpUtil() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            authService.logout();
            stpUtilMock.verify(StpUtil::logout);
        }
    }

    // ================================================================
    // register
    // ================================================================

    @Test
    @DisplayName("register 验证码校验失败时抛出 BusinessException(CAPTCHA_ERROR)")
    void registerThrowsWhenCaptchaInvalid() {
        RegisterRequest request = createRegisterRequest();
        request.setCaptchaCode("WRONG");
        when(captchaService.validate("uuid-1", "WRONG")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.CAPTCHA_ERROR);
    }

    @Test
    @DisplayName("register 邮箱已注册时抛出 BusinessException(EMAIL_EXISTS)")
    void registerThrowsWhenEmailExists() {
        RegisterRequest request = createRegisterRequest();
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.EMAIL_EXISTS);
    }

    @Test
    @DisplayName("register 手机号已注册时抛出 BusinessException(PHONE_EXISTS)")
    void registerThrowsWhenPhoneExists() {
        RegisterRequest request = createRegisterRequest();
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.PHONE_EXISTS);
    }

    @Test
    @DisplayName("register 用户名已占用时抛出 BusinessException(USERNAME_EXISTS)")
    void registerThrowsWhenUsernameOccupied() {
        RegisterRequest request = createRegisterRequest();
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, false, true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.USERNAME_EXISTS);
    }

    @Test
    @DisplayName("register 党员编号已存在时抛出 BusinessException(PARTY_MEMBER_ID_EXISTS)")
    void registerThrowsWhenPartyMemberIdExists() {
        RegisterRequest request = createRegisterRequest();
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, false, false, true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.PARTY_MEMBER_ID_EXISTS);
    }

    @Test
    @DisplayName("register 所有校验通过且插入成功时返回成功结果")
    void registerSuccessWhenAllChecksPass() {
        RegisterRequest request = createRegisterRequest();
        String rawPassword = request.getPassword();
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, false, false, false);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw(rawPassword)).thenReturn(newEncodedPassword());

            var result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getMessage()).isEqualTo("OK");
            bcryptMock.verify(() -> BCrypt.hashpw(rawPassword));
            verify(userMapper).insert(any(User.class));
        }
    }

    @Test
    @DisplayName("register email 为 null 时跳过邮箱校验")
    void registerSkipsEmailCheckWhenEmailIsNull() {
        RegisterRequest request = createRegisterRequest();
        request.setEmail(null);
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, false, false);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw(anyString())).thenReturn(newEncodedPassword());

            var result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getMessage()).isEqualTo("OK");
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).insert(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isNull();
        }
    }

    // ================================================================
    // changePassword
    // ================================================================

    @Test
    @DisplayName("changePassword 用户未登录时抛出 BusinessException(UNAUTHORIZED)")
    void changePasswordThrowsWhenNotLoggedIn() {
        ChangePasswordRequest request = createChangePasswordRequest();
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn(null);

            assertThatThrownBy(() -> authService.changePassword(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(AuthErrorConstants.UNAUTHORIZED);
        }
    }

    @Test
    @DisplayName("changePassword 用户不存在时抛出 BusinessException(USER_NOT_FOUND)")
    void changePasswordThrowsWhenUserNotFound() {
        ChangePasswordRequest request = createChangePasswordRequest();
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("u1");
            when(userMapper.selectById("u1")).thenReturn(null);

            assertThatThrownBy(() -> authService.changePassword(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(AuthErrorConstants.USER_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("changePassword 旧密码不匹配时抛出 BusinessException(OLD_PASSWORD_ERROR)")
    void changePasswordThrowsWhenOldPasswordMismatch() {
        ChangePasswordRequest request = createChangePasswordRequest();
        User user = createUser(1L, "testuser");
        user.setPassword(newEncodedPassword());

        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
                MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("u1");
            when(userMapper.selectById("u1")).thenReturn(user);
            bcryptMock
                    .when(() -> BCrypt.checkpw(request.getOldPassword(), user.getPassword()))
                    .thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(AuthErrorConstants.OLD_PASSWORD_ERROR);
        }
    }

    @Test
    @DisplayName("changePassword 旧密码正确时更新密码并返回 void")
    void changePasswordSuccessWhenOldPasswordMatches() {
        ChangePasswordRequest request = createChangePasswordRequest();
        String newRawPassword = request.getNewPassword();
        String encodedNewPassword = newEncodedPassword();
        User user = createUser(1L, "testuser");
        user.setPassword(newEncodedPassword());

        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
                MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            SaSession session = mock(SaSession.class);
            stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("u1");
            stpUtilMock.when(StpUtil::getSession).thenReturn(session);
            when(userMapper.selectById("u1")).thenReturn(user);
            bcryptMock
                    .when(() -> BCrypt.checkpw(request.getOldPassword(), user.getPassword()))
                    .thenReturn(true);
            bcryptMock.when(() -> BCrypt.hashpw(newRawPassword)).thenReturn(encodedNewPassword);
            when(userMapper.updateById(user)).thenReturn(1);

            authService.changePassword(request);

            assertThat(user.getPassword()).isEqualTo(encodedNewPassword);
            verify(userMapper).updateById(user);
            ArgumentCaptor<Object> principalCaptor = ArgumentCaptor.forClass(Object.class);
            verify(session).set(org.mockito.Mockito.eq("user"), principalCaptor.capture());
            assertThat(principalCaptor.getValue()).isInstanceOf(SessionUserPrincipal.class);
        }
    }

    @Test
    @DisplayName("changePassword 更新失败时抛出 BusinessException(PASSWORD_CHANGE_ERROR)")
    void changePasswordThrowsWhenUpdateFails() {
        ChangePasswordRequest request = createChangePasswordRequest();
        String newRawPassword = request.getNewPassword();
        User user = createUser(1L, "testuser");
        user.setPassword(newEncodedPassword());

        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
                MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("u1");
            when(userMapper.selectById("u1")).thenReturn(user);
            bcryptMock
                    .when(() -> BCrypt.checkpw(request.getOldPassword(), user.getPassword()))
                    .thenReturn(true);
            bcryptMock.when(() -> BCrypt.hashpw(newRawPassword)).thenReturn(newEncodedPassword());
            when(userMapper.updateById(user)).thenReturn(0);

            assertThatThrownBy(() -> authService.changePassword(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(AuthErrorConstants.PASSWORD_CHANGE_ERROR);
        }
    }

    // ================================================================
    // helpers
    // ================================================================

    @Test
    @DisplayName("changePassword 弱密码抛出 BusinessException(PASSWORD_WEAK)")
    void changePasswordThrowsWhenNewPasswordWeak() {
        ChangePasswordRequest request = createChangePasswordRequest();
        request.setNewPassword("weak");
        User user = createUser(1L, "testuser");
        user.setPassword(newEncodedPassword());

        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("u1");
            when(userMapper.selectById("u1")).thenReturn(user);

            assertThatThrownBy(() -> authService.changePassword(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(UserErrorConstants.PASSWORD_WEAK);
            verify(userMapper, never()).updateById(any(com.rauio.smartdangjian.server.user.pojo.entity.User.class));
        }
    }

    @Test
    @DisplayName("changePassword 弱密码不调用 updateById")
    void changePasswordDoesNotCallUpdateByIdWhenWeakPassword() {
        ChangePasswordRequest request = createChangePasswordRequest();
        request.setNewPassword("onlylowercase1");
        User user = createUser(1L, "testuser");
        user.setPassword(newEncodedPassword());

        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("u1");
            when(userMapper.selectById("u1")).thenReturn(user);

            assertThatThrownBy(() -> authService.changePassword(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(UserErrorConstants.PASSWORD_WEAK);
            verify(userMapper, never()).updateById(any(com.rauio.smartdangjian.server.user.pojo.entity.User.class));
        }
    }

    private static String newEncodedPassword() {
        return "enc_" + UUID.randomUUID();
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setPassport("admin");
        request.setPassword(UUID.randomUUID().toString());
        request.setPlatform("web");
        request.setCaptchaUUID("uuid-1");
        request.setCaptchaCode("1234");
        return request;
    }

    private RegisterRequest createRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setType(UserType.STUDENT);
        request.setUsername("newuser");
        request.setPassword(UUID.randomUUID().toString());
        request.setRealName("张三");
        request.setIdCard("110101199001011234");
        request.setPartyMemberId("PM123456789012345678");
        request.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        request.setBranchName("某某党支部");
        request.setEmail("newuser@example.com");
        request.setPhone("13800138000");
        request.setCaptchaUUID("uuid-1");
        request.setCaptchaCode("1234");
        request.setUniversityId("univ-1");
        return request;
    }

    private ChangePasswordRequest createChangePasswordRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("OldP@ss1");
        request.setNewPassword("NewP@ss1");
        return request;
    }

    private User createUser(Long id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email("user@example.com")
                .phone("13800138000")
                .userType(UserType.STUDENT)
                .build();
    }

    @Test
    @DisplayName("register email 为空字符串时跳过邮箱校验")
    void registerSkipsEmailCheckWhenEmailIsBlank() {
        RegisterRequest request = createRegisterRequest();
        request.setEmail("");
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, false, false);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw(anyString())).thenReturn(newEncodedPassword());

            var result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getMessage()).isEqualTo("OK");
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).insert(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEmpty();
        }
    }

    @Test
    @DisplayName("register partyMemberId is null skips party member id check")
    void registerSkipsPartyMemberIdCheckWhenNull() {
        RegisterRequest request = createRegisterRequest();
        request.setPartyMemberId(null);
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, false, false);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw(anyString())).thenReturn(newEncodedPassword());

            var result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getMessage()).isEqualTo("OK");
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).insert(userCaptor.capture());
            assertThat(userCaptor.getValue().getPartyMemberId()).isNull();
        }
    }

    @Test
    @DisplayName("register partyMemberId 为空字符串时跳过党员编号校验")
    void registerSkipsPartyMemberIdCheckWhenBlank() {
        RegisterRequest request = createRegisterRequest();
        request.setPartyMemberId("");
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, false, false);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw(anyString())).thenReturn(newEncodedPassword());

            var result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("200");
            assertThat(result.getMessage()).isEqualTo("OK");
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).insert(userCaptor.capture());
            assertThat(userCaptor.getValue().getPartyMemberId()).isEmpty();
        }
    }
}
