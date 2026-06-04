package com.rauio.smartdangjian.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.rauio.smartdangjian.server.auth.config.AuthProperties;
import com.rauio.smartdangjian.server.auth.pojo.Captcha;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaptchaServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private Environment env;

    @Mock
    private AuthProperties authProperties;

    @Mock
    private AuthProperties.Captcha captcha;

    @InjectMocks
    private CaptchaService captchaService;

    private ValueOperations<String, Object> valueOps;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(env.getActiveProfiles()).thenReturn(new String[0]);
        when(authProperties.getCaptcha()).thenReturn(captcha);
    }

    @Test
    @DisplayName("get 返回验证码对象且 code 字段为 null")
    void getReturnsCaptchaWithNullCode() {
        Captcha result = captchaService.get();

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isNotBlank();
        assertThat(result.getCode()).isNull();
        assertThat(result.getBase64()).isNotBlank();
    }

    @Test
    @DisplayName("generate 生成验证码并写入 Redis")
    void generateCreatesCaptchaAndStoresInRedis() {
        Captcha result = captchaService.generate();

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isNotBlank();
        assertThat(result.getCode()).isNotBlank();
        assertThat(result.getBase64()).isNotBlank();
        verify(valueOps).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("generate 两次生成的 UUID 不同")
    void generateProducesUniqueUuids() {
        Captcha first = captchaService.generate();
        Captcha second = captchaService.generate();

        assertThat(first.getUuid()).isNotEqualTo(second.getUuid());
    }

    @Test
    @DisplayName("validate dev profile 下 testCode 配置不为空且匹配时直接返回 true")
    void validateReturnsTrueWhenTestCodeMatches() {
        when(captcha.getTestCode()).thenReturn("9999");
        when(env.getActiveProfiles()).thenReturn(new String[] {"dev"});

        Boolean result = captchaService.validate("any-uuid", "9999");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validate testCode 配置不为空但不匹配时继续校验 Redis")
    void validateFallsThroughToRedisWhenTestCodeMismatches() {
        when(valueOps.get(eq("captcha:uuid-1"))).thenReturn("1234");

        Boolean result = captchaService.validate("uuid-1", "1234");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validate Redis 中验证码匹配时返回 true")
    void validateReturnsTrueWhenRedisCodeMatches() {
        when(valueOps.get(eq("captcha:my-uuid"))).thenReturn("ABCD");

        Boolean result = captchaService.validate("my-uuid", "ABCD");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validate Redis 中验证码不匹配时返回 false")
    void validateReturnsFalseWhenRedisCodeMismatches() {
        when(valueOps.get(eq("captcha:my-uuid"))).thenReturn("ABCD");

        Boolean result = captchaService.validate("my-uuid", "WRONG");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validate Redis 中无验证码时返回 false")
    void validateReturnsFalseWhenRedisHasNoCode() {
        when(valueOps.get(eq("captcha:my-uuid"))).thenReturn(null);

        Boolean result = captchaService.validate("my-uuid", "ABCD");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validate testCode is blank non-null falls through to Redis")
    void validateBlankTestCodeFallsThroughToRedis() {
        when(valueOps.get(eq("captcha:uuid-1"))).thenReturn("1234");

        Boolean result = captchaService.validate("uuid-1", "1234");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validate prod profile 下 testCode 匹配也不走旁路且 Redis 不符则返回 false")
    void validateTestCodeBypassBlockedWhenProdProfile() {
        when(env.getActiveProfiles()).thenReturn(new String[] {"prod"});
        when(valueOps.get(eq("captcha:any-uuid"))).thenReturn("0000");

        Boolean result = captchaService.validate("any-uuid", "9999");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validate test profile 下允许使用测试码")
    void validateTestCodeBypassAllowedWhenTestProfile() {
        when(captcha.getTestCode()).thenReturn("9999");
        when(env.getActiveProfiles()).thenReturn(new String[] {"test"});

        Boolean result = captchaService.validate("any-uuid", "9999");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validate code 为 null 时返回 false")
    void validateReturnsFalseWhenCodeIsNull() {
        when(valueOps.get(eq("captcha:my-uuid"))).thenReturn("ABCD");

        Boolean result = captchaService.validate("my-uuid", null);

        assertThat(result).isFalse();
    }
}
