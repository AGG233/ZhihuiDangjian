package com.rauio.smartdangjian.crosslayer.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.auth.service.RefreshTokenService;
import com.rauio.smartdangjian.server.auth.service.RefreshTokenService.TokenIdentity;

/**
 * 刷新令牌 Lua 消费链路跨层回归：装配真实 {@link StringRedisTemplate}（Lettuce 连本地 Redis）
 * 与 {@link RefreshTokenService}，验证原子消费脚本在真实 Redis 序列化环境下
 * 「签发 → 消费 → 重放检测」全链路正确。
 *
 * <p>回归背景：Lua 脚本返回的字面量状态码经 JSON 序列化器反序列化会抛
 * {@code Unrecognized token 'OK'}，导致刷新接口 500——本测试以真实序列化环境守护该修复。
 */
@SpringBootTest(
        classes = RefreshTokenLuaCrossLayerTest.Config.class,
        properties = "spring.data.redis.repositories.enabled=false")
class RefreshTokenLuaCrossLayerTest {

    @SpringBootConfiguration
    @Import(RefreshTokenService.class)
    static class Config {

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", 6379);
            factory.setDatabase(0);
            return factory;
        }

        @Bean
        StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
            return new StringRedisTemplate(factory);
        }
    }

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("真实 Redis 下 issue→consume 原子返回归属用户与平台")
    void issueConsumeRoundTripOnRealRedis() {
        String token = refreshTokenService.issue(42L, "app");

        TokenIdentity identity = refreshTokenService.consume(token);

        assertThat(identity.userId()).isEqualTo(42L);
        assertThat(identity.platform()).isEqualTo("app");
    }

    @Test
    @DisplayName("已作废令牌再提交触发重放检测并吊销全部会话")
    void replayedTokenTriggersReuseRevocation() {
        String token = refreshTokenService.issue(43L, "web");
        refreshTokenService.consume(token);

        assertThatThrownBy(() -> refreshTokenService.consume(token))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.REFRESH_TOKEN_REUSE);
    }

    @Test
    @DisplayName("未知令牌走 EXPIRED 分支抛业务异常而非系统错误")
    void unknownTokenThrowsBusinessException() {
        assertThatThrownBy(() -> refreshTokenService.consume("nonexistent-smoke-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.REFRESH_TOKEN_EXPIRED);
    }
}
