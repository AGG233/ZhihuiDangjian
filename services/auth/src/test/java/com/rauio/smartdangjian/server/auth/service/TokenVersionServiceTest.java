package com.rauio.smartdangjian.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class TokenVersionServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private TokenVersionService tokenVersionService;

    @Test
    @DisplayName("current 返回 Redis 中的当前版本号")
    void currentReturnsStoredVersion() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:ver:42")).thenReturn(3L);

        assertThat(tokenVersionService.current(42L)).isEqualTo(3L);
    }

    @Test
    @DisplayName("current 未初始化时返回 0")
    void currentDefaultsToZeroWhenAbsent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);

        assertThat(tokenVersionService.current(42L)).isZero();
    }

    @Test
    @DisplayName("bump 递增并返回新版本号")
    void bumpIncrementsAndReturnsNewVersion() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(valueOps.increment("auth:ver:42")).thenReturn(4L);

        assertThat(tokenVersionService.bump(42L)).isEqualTo(4L);
        verify(valueOps).increment("auth:ver:42");
    }
}
