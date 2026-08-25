package com.rauio.smartdangjian.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import com.rauio.smartdangjian.constants.RedisConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
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

    private void stubConsumeResult(String status, String owner) {
        lenient()
                .when(redisTemplate.execute(any(RedisScript.class), anyList(), any(String.class)))
                .thenReturn((List) List.of(status, owner));
    }

    @Test
    @DisplayName("issue 写入「userId:platform」指纹键并返回令牌明文")
    void issueStoresFingerprintAndReturnsToken() {
        String token = refreshTokenService.issue(7L, "app");

        assertThat(token).isNotBlank();
        verify(valueOps)
                .set(
                        org.mockito.ArgumentMatchers.eq(storedKeyOf(token)),
                        org.mockito.ArgumentMatchers.eq("7:app"),
                        org.mockito.ArgumentMatchers.eq(RefreshTokenService.REFRESH_TOKEN_TTL));
    }

    @Test
    @DisplayName("consume 有效令牌原子返回归属用户与平台")
    void consumeReturnsIdentityForValidToken() {
        String token = UUID.randomUUID().toString();
        stubConsumeResult("OK", "42:app");

        RefreshTokenService.TokenIdentity identity = refreshTokenService.consume(token);

        assertThat(identity.userId()).isEqualTo(42L);
        assertThat(identity.platform()).isEqualTo("app");
    }

    @Test
    @DisplayName("consume 未知或过期令牌抛 REFRESH_TOKEN_EXPIRED")
    void consumeThrowsWhenTokenUnknown() {
        stubConsumeResult("EXPIRED", "");

        assertThatThrownBy(() -> refreshTokenService.consume("missing-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("issue 平台缺省时按 web 写入归属值")
    void issueDefaultsPlatformToWeb() {
        String token = refreshTokenService.issue(7L, null);

        verify(valueOps)
                .set(
                        org.mockito.ArgumentMatchers.eq(storedKeyOf(token)),
                        org.mockito.ArgumentMatchers.eq("7:web"),
                        org.mockito.ArgumentMatchers.eq(RefreshTokenService.REFRESH_TOKEN_TTL));
    }

    @Test
    @DisplayName("consume 历史纯 userId 归属值按 web 平台解析")
    void consumeParsesLegacyOwnerWithoutPlatform() {
        stubConsumeResult("OK", "42");

        RefreshTokenService.TokenIdentity identity =
                refreshTokenService.consume(UUID.randomUUID().toString());

        assertThat(identity.userId()).isEqualTo(42L);
        assertThat(identity.platform()).isEqualTo("web");
    }

    @Test
    @DisplayName("consume 兼容滚动升级残留的 JSON 引号包装归属值")
    void consumeToleratesLegacyJsonQuotedOwner() {
        stubConsumeResult("OK", "\"42:app\"");

        RefreshTokenService.TokenIdentity identity =
                refreshTokenService.consume(UUID.randomUUID().toString());

        assertThat(identity.userId()).isEqualTo(42L);
        assertThat(identity.platform()).isEqualTo("app");
    }

    @Test
    @DisplayName("consume 重放的历史纯 userId 归属值同样触发全量吊销")
    void consumeReplayLegacyOwnerStillRevokesAll() {
        Cursor<String> emptyCursor = mock(Cursor.class);
        when(emptyCursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(emptyCursor);
        stubConsumeResult("REUSED", "42");

        assertThatThrownBy(() -> refreshTokenService.consume("legacy-replayed"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.REFRESH_TOKEN_REUSE);

        verify(redisTemplate, atLeastOnce()).scan(any(ScanOptions.class));
    }

    @Test
    @DisplayName("consume 脚本无返回时按过期令牌处理")
    void consumeTreatsEmptyScriptResultAsExpired() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(String.class)))
                .thenReturn(null);

        assertThatThrownBy(() -> refreshTokenService.consume("null-result"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("consume 已作废令牌再次提交判定泄露并吊销该用户全部刷新令牌")
    void consumeDetectsReplayAndRevokesAll() {
        Cursor<String> emptyCursor = mock(Cursor.class);
        when(emptyCursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(emptyCursor);
        stubConsumeResult("REUSED", "42:web");

        assertThatThrownBy(() -> refreshTokenService.consume("replayed-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.REFRESH_TOKEN_REUSE);

        verify(redisTemplate, atLeastOnce()).scan(any(ScanOptions.class));
    }

    @Test
    @DisplayName("revokeAllForUser 按 userId 前缀匹配，仅删除归属于该用户的键（含历史纯 userId 值）")
    void revokeAllDeletesOnlyOwnedKeys() {
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, true, true, true, false);
        when(cursor.next())
                .thenReturn("auth:refresh:aaa", "auth:refresh:bbb", "auth:refresh:ccc", "auth:refresh:ddd", null);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        lenient()
                .when(redisTemplate.type(anyString()))
                .thenReturn(org.springframework.data.redis.connection.DataType.STRING);
        when(valueOps.get("auth:refresh:aaa")).thenReturn("42:app");
        when(valueOps.get("auth:refresh:bbb")).thenReturn("99:web");
        when(valueOps.get("auth:refresh:ccc")).thenReturn("42");
        when(valueOps.get("auth:refresh:ddd")).thenReturn(null);

        refreshTokenService.revokeAllForUser(42L);

        verify(redisTemplate).delete("auth:refresh:aaa");
        verify(redisTemplate).delete("auth:refresh:ccc");
        verify(redisTemplate, never()).delete("auth:refresh:bbb");
        verify(redisTemplate, never()).delete("auth:refresh:ddd");
    }

    @Test
    @DisplayName("revokeAllForUser 跳过同前缀下非 string 类型的历史遗留键")
    void revokeAllSkipsNonStringKeys() {
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn("auth:refresh:user:42", null);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(redisTemplate.type("auth:refresh:user:42"))
                .thenReturn(org.springframework.data.redis.connection.DataType.SET);

        refreshTokenService.revokeAllForUser(42L);

        verify(valueOps, never()).get(anyString());
        verify(redisTemplate, never()).delete(anyString());
    }
}
