package com.rauio.smartdangjian.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.service.JwtService;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("JwtService 单元测试")
class JwtServiceTest {

    @Test
    @DisplayName("登出后的 token 再次校验必须失败")
    void validateTokenRejectsBlacklistedToken() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        UserMapper userMapper = mock(UserMapper.class);
        JwtService jwtService = new JwtService(redisTemplate, userMapper, new ObjectMapper());
        User user = User.builder()
                .id("user-001")
                .username("alice")
                .userType(UserType.STUDENT)
                .build();
        String token = jwtService.generateAccessToken(user, JwtService.PLATFORM_WEB);
        ValueOperations<String, String> ops = mock(ValueOperations.class);

        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("令牌已失效");
        verify(userMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("有效 token 未命中黑名单时通过校验")
    void validateTokenAcceptsValidToken() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        UserMapper userMapper = mock(UserMapper.class);
        JwtService jwtService = new JwtService(redisTemplate, userMapper, new ObjectMapper());
        User user = User.builder()
                .id("user-001")
                .username("alice")
                .userType(UserType.STUDENT)
                .build();
        String token = jwtService.generateAccessToken(user, JwtService.PLATFORM_WEB);
        ValueOperations<String, String> ops = mock(ValueOperations.class);

        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("user:data:user-001")).thenReturn(null);
        when(userMapper.selectById("user-001")).thenReturn(user);

        assertThatCode(() -> jwtService.validateToken(token)).doesNotThrowAnyException();
        verify(ops).set(eq("user:data:user-001"), any(String.class), eq(3600000L), any());
    }
}
