package com.rauio.smartdangjian.crosslayer.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.auth.config.SaTokenJwtConfig;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.response.LoginResponse;
import com.rauio.smartdangjian.server.auth.service.AuthService;
import com.rauio.smartdangjian.server.auth.service.CaptchaService;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;

/**
 * 认证 JWT 跨层回归：装配真实 {@link SaTokenJwtConfig} + {@link AuthService}，
 * 验证登录签发的是三段式 JWT，且能通过 {@link StpUtil#getLoginIdByToken(String)}
 * 反解出 loginId，确保认证核心从随机 token 切换到 JWT 后整条登录链路仍然正确。
 *
 * <p>仅验证码通过 mocked RedisTemplate 消费，会话存储使用 Sa-Token 官方 mock 上下文，
 * 不依赖真实 Redis / Web 容器。
 */
@SpringBootTest(
        classes = AuthJwtCrossLayerTest.TestConfig.class,
        properties = "spring.data.redis.repositories.enabled=false")
class AuthJwtCrossLayerTest extends CrossLayerTestBase {

    private static final String JWT_SECRET_KEY = "test-jwt-secret-key-0123456789abcdef";

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AuthService authService;

    @Autowired
    private CaptchaService captchaService;

    private final String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

    @SpringBootConfiguration
    @Import(SaTokenJwtConfig.class)
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
    void setUpJwtContext() {
        // mock 上下文供 Sa-Token 在无 Web 容器/Redis 时工作（登录与会话读写均走内存）
        SaTokenContextMockUtil.setMockContext();
        SaManager.getConfig().setJwtSecretKey(JWT_SECRET_KEY);
        captchaService.setTestCode("TEST8888");
        // mocked RedisTemplate：opsForValue() 返回空操作，get() 默认返回 null（无失败计数）
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(redisTemplate.delete(anyString())).thenReturn(true);
    }

    @AfterEach
    void resetJwtContext() {
        SaManager.getConfig().setJwtSecretKey(null);
    }

    @Test
    @DisplayName("登录签发三段式 JWT，且 StpUtil 可按 token 反解 loginId")
    void loginIssuesThreePartJwtAndResolvesLoginId() {
        User user = User.builder()
                .id(42L)
                .username(unique)
                .password(BCrypt.hashpw("real-password"))
                .status(AccountStatus.ACTIVE)
                .build();
        when(userService.getByPassport(unique)).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setPassport(unique);
        request.setPassword("real-password");
        request.setCaptchaUUID(unique);
        request.setCaptchaCode("TEST8888");
        request.setPlatform("web");

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getAccessToken().split("\\.")).hasSize(3);

        Object loginId = StpUtil.getLoginIdByToken(response.getAccessToken());
        assertThat(String.valueOf(loginId)).isEqualTo("42");
    }
}
