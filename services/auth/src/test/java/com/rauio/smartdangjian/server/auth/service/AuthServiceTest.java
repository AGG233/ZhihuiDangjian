package com.rauio.smartdangjian.server.auth.service;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.pojo.response.LoginResponse;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
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
    @DisplayName("login 验证码通过且认证成功时返回 LoginResponse")
    void loginReturnsTokenWhenSuccessful() {
        LoginRequest request = createLoginRequest();
        User user = createUser("u1", "testuser");
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(user, "web")).thenReturn("jwt-token-abc");

        LoginResponse result = authService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt-token-abc");
    }

    @Test
    @DisplayName("login 认证过程抛出异常时抛出 BusinessException(PASSWORD_ERROR)")
    void loginThrowsWhenAuthenticationFails() {
        LoginRequest request = createLoginRequest();
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("auth failed"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.PASSWORD_ERROR);
    }

    // ================================================================
    // logout
    // ================================================================

    @Test
    @DisplayName("logout 委托给 jwtService.logout")
    void logoutDelegatesToJwtService() {
        authService.logout("some-token");

        verify(jwtService).logout("some-token");
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
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, false, false, false);
        when(passwordEncoder.encode("Test@1234")).thenReturn("encodedPass");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        var result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
        verify(passwordEncoder).encode("Test@1234");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("register email 为 null 时跳过邮箱校验")
    void registerSkipsEmailCheckWhenEmailIsNull() {
        RegisterRequest request = createRegisterRequest();
        request.setEmail(null);
        when(captchaService.validate("uuid-1", "1234")).thenReturn(true);
        when(userMapper.exists(any())).thenReturn(false, false, false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        var result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
    }

    // ================================================================
    // changePassword
    // ================================================================

    @Test
    @DisplayName("changePassword 用户未登录时抛出 BusinessException(UNAUTHORIZED)")
    void changePasswordThrowsWhenNotLoggedIn() {
        ChangePasswordRequest request = createChangePasswordRequest();
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(null);

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.UNAUTHORIZED);
    }

    @Test
    @DisplayName("changePassword 用户不存在时抛出 BusinessException(USER_NOT_FOUND)")
    void changePasswordThrowsWhenUserNotFound() {
        ChangePasswordRequest request = createChangePasswordRequest();
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("u1");
        when(userMapper.selectById("u1")).thenReturn(null);

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("changePassword 旧密码不匹配时抛出 BusinessException(OLD_PASSWORD_ERROR)")
    void changePasswordThrowsWhenOldPasswordMismatch() {
        ChangePasswordRequest request = createChangePasswordRequest();
        User user = createUser("u1", "testuser");
        user.setPassword("encodedOldPassword");

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("u1");
        when(userMapper.selectById("u1")).thenReturn(user);
        when(passwordEncoder.matches("wrongOldPass", "encodedOldPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.OLD_PASSWORD_ERROR);
    }

    @Test
    @DisplayName("changePassword 旧密码正确时更新密码并返回 true")
    void changePasswordSuccessWhenOldPasswordMatches() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("correctOldPass");
        request.setNewPassword("newSecretPass");
        User user = createUser("u1", "testuser");
        user.setPassword("encodedOldPassword");

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("u1");
        when(userMapper.selectById("u1")).thenReturn(user);
        when(passwordEncoder.matches("correctOldPass", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newSecretPass")).thenReturn("encodedNewPassword");
        when(userMapper.updateById(user)).thenReturn(1);

        Boolean result = authService.changePassword(request);

        assertThat(result).isTrue();
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        verify(jwtService).clearUserCache("u1");
        verify(userMapper).updateById(user);
    }

    @Test
    @DisplayName("changePassword 更新失败时返回 false")
    void changePasswordReturnsFalseWhenUpdateFails() {
        ChangePasswordRequest request = createChangePasswordRequest();
        User user = createUser("u1", "testuser");
        user.setPassword("encodedOldPassword");

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("u1");
        when(userMapper.selectById("u1")).thenReturn(user);
        when(passwordEncoder.matches("wrongOldPass", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newSecretPass")).thenReturn("encodedNewPassword");
        when(userMapper.updateById(user)).thenReturn(0);

        Boolean result = authService.changePassword(request);

        assertThat(result).isFalse();
    }

    // ================================================================
    // helpers
    // ================================================================

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setPassport("admin");
        request.setPassword("password123");
        request.setPlatform("web");
        request.setCaptchaUUID("uuid-1");
        request.setCaptchaCode("1234");
        return request;
    }

    private RegisterRequest createRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setType(UserType.STUDENT);
        request.setUsername("newuser");
        request.setPassword("Test@1234");
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
        request.setOldPassword("wrongOldPass");
        request.setNewPassword("newSecretPass");
        return request;
    }

    private User createUser(String id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email("user@example.com")
                .phone("13800138000")
                .userType(UserType.STUDENT)
                .build();
    }
}
