package com.rauio.smartdangjian.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import com.rauio.smartdangjian.constants.RedisConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private String storedKeyOf(String token) {
        // 与实现一致的指纹推导：SHA-256 hex
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return RedisConstants.AUTH_REFRESH_TOKEN_PREFIX + hex;
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("issue 写入指纹键并返回令牌明文")
    void issueStoresFingerprintAndReturnsToken() {
        String token = refreshTokenService.issue(7L);

        assertThat(token).isNotBlank();
        verify(valueOps)
                .set(
                        org.mockito.ArgumentMatchers.eq(storedKeyOf(token)),
                        org.mockito.ArgumentMatchers.eq("7"),
                        org.mockito.ArgumentMatchers.eq(RefreshTokenService.REFRESH_TOKEN_TTL));
    }

    @Test
    @DisplayName("validate 有效令牌返回归属用户 ID")
    void validateReturnsOwnerForValidToken() {
        String token = UUID.randomUUID().toString();
        when(valueOps.get(storedKeyOf(token))).thenReturn("42");
        when(valueOps.get(contains(RedisConstants.AUTH_REFRESH_TOKEN_USED_PREFIX)))
                .thenReturn(null);

        assertThat(refreshTokenService.validate(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("validate 未知或过期令牌抛 REFRESH_TOKEN_EXPIRED")
    void validateThrowsWhenTokenUnknown() {
        when(valueOps.get(anyString())).thenReturn(null);

        assertThatThrownBy(() -> refreshTokenService.validate("missing-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("validate 已作废令牌再次提交判定泄露并吊销该用户全部刷新令牌")
    void validateDetectsReplayAndRevokesAll() {
        Cursor<String> emptyCursor = mock(Cursor.class);
        when(emptyCursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(emptyCursor);
        when(valueOps.get(contains(RedisConstants.AUTH_REFRESH_TOKEN_USED_PREFIX)))
                .thenReturn("42");

        assertThatThrownBy(() -> refreshTokenService.validate("replayed-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.REFRESH_TOKEN_REUSE);

        verify(redisTemplate, org.mockito.Mockito.atLeastOnce()).scan(any(ScanOptions.class));
    }

    @Test
    @DisplayName("rotate 删除旧指纹、写入已用标记并签发新令牌")
    void rotateDeletesOldAndIssuesNew() {
        String oldToken = UUID.randomUUID().toString();

        String newToken = refreshTokenService.rotate(oldToken, 42L);

        verify(redisTemplate).delete(storedKeyOf(oldToken));
        verify(valueOps)
                .set(
                        contains(RedisConstants.AUTH_REFRESH_TOKEN_USED_PREFIX),
                        org.mockito.ArgumentMatchers.eq("42"),
                        org.mockito.ArgumentMatchers.eq(RefreshTokenService.REFRESH_TOKEN_TTL));
        verify(valueOps)
                .set(
                        org.mockito.ArgumentMatchers.eq(storedKeyOf(newToken)),
                        org.mockito.ArgumentMatchers.eq("42"),
                        org.mockito.ArgumentMatchers.eq(RefreshTokenService.REFRESH_TOKEN_TTL));
        assertThat(newToken).isNotEqualTo(oldToken);
    }

    @Test
    @DisplayName("revokeAllForUser 仅删除归属于该用户的键")
    void revokeAllDeletesOnlyOwnedKeys() {
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("auth:refresh:aaa", "auth:refresh:bbb", null);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(valueOps.get("auth:refresh:aaa")).thenReturn("42");
        when(valueOps.get("auth:refresh:bbb")).thenReturn("99");

        refreshTokenService.revokeAllForUser(42L);

        verify(redisTemplate).delete("auth:refresh:aaa");
        verify(redisTemplate, never()).delete("auth:refresh:bbb");
    }
}
