package com.rauio.smartdangjian.crosslayer.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.auth.pojo.Captcha;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.service.AuthService;
import com.rauio.smartdangjian.server.auth.service.CaptchaService;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 认证安全跨层回归：真实 AuthService + 真实 Redis（验证码消费、登录失败锁定），
 * 验证 P0 安全修复在真实依赖链路上生效。
 */
@SpringBootTest(classes = AuthSecurityCrossLayerTest.TestConfig.class)
class AuthSecurityCrossLayerTest extends CrossLayerTestBase {

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String UNIQUE = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        CaptchaService captchaService(RedisTemplate<String, Object> redisTemplate) {
            return new CaptchaService(redisTemplate);
        }

        @Bean
        AuthService authService(
                CaptchaService captchaService,
                UserMapper userMapper,
                UserService userService,
                RedisTemplate<String, Object> redisTemplate) {
            return new AuthService(captchaService, userMapper, userService, redisTemplate);
        }
    }

    @BeforeEach
    void disableTestCodeShortcut() {
        // 关闭 TEST8888 短路，确保验证码校验走真实 Redis 消费路径
        captchaService.setTestCode(null);
    }

    @AfterEach
    void cleanUpRedisKeys() {
        redisTemplate.delete("captcha:" + UNIQUE);
        redisTemplate.delete("login:fail:" + UNIQUE);
    }

    @Test
    @DisplayName("注册 MANAGER 类型被拒绝（防匿名提权）")
    void registerRejectsManagerType() {
        captchaService.setTestCode("TEST8888");
        RegisterRequest request = new RegisterRequest();
        request.setType(UserType.MANAGER);
        request.setUsername("evil-" + UNIQUE);
        request.setPassword("Passw0rd!");
        request.setCaptchaUUID(UNIQUE);
        request.setCaptchaCode("TEST8888");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.REGISTER_TYPE_FORBIDDEN);
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("验证码校验通过后被消费，同 uuid 二次校验失败（防重放）")
    void captchaIsConsumedAfterValidation() {
        Captcha captcha = captchaService.generate();
        String uuid = captcha.getUuid();
        String code = captcha.getCode();

        assertThat(captchaService.validate(uuid, code)).isTrue();
        assertThat(captchaService.validate(uuid, code)).isFalse();
    }

    @Test
    @DisplayName("验证码校验失败同样消费，无法在同一验证码上爆破")
    void captchaIsConsumedEvenOnMismatch() {
        Captcha captcha = captchaService.generate();

        assertThat(captchaService.validate(captcha.getUuid(), "0000")).isFalse();
        assertThat(captchaService.validate(captcha.getUuid(), captcha.getCode()))
                .isFalse();
    }

    @Test
    @DisplayName("连续 5 次登录失败后第 6 次被锁定（真实 Redis 计数）")
    void loginLocksAccountAfterFiveFails() {
        captchaService.setTestCode("TEST8888");
        User user = User.builder()
                .id(1L)
                .username(UNIQUE)
                .password(BCrypt.hashpw("real-bcrypt-hash"))
                .status(AccountStatus.ACTIVE)
                .build();
        when(userService.getByPassport(UNIQUE)).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setPassport(UNIQUE);
        request.setPassword("wrong-password");
        request.setCaptchaUUID(UNIQUE);
        request.setCaptchaCode("TEST8888");

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(AuthErrorConstants.LOGIN_FAILED);
        }

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.ACCOUNT_LOCKED);
        // 锁定后不再查询用户
        verify(userService, org.mockito.Mockito.times(5)).getByPassport(anyString());
    }

    @Test
    @DisplayName("用户不存在与密码错误返回同一错误码（防枚举）")
    void loginErrorIsUniformForUnknownUser() {
        captchaService.setTestCode("TEST8888");
        when(userService.getByPassport(UNIQUE)).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setPassport(UNIQUE);
        request.setPassword("whatever");
        request.setCaptchaUUID(UNIQUE);
        request.setCaptchaCode("TEST8888");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.LOGIN_FAILED);
    }
}
