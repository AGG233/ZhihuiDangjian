package com.rauio.smartdangjian.server.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.constants.RedisConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;

import lombok.RequiredArgsConstructor;

/**
 * 刷新令牌服务（无状态 JWT 双令牌体系的可吊销侧）。
 *
 * <p>刷新令牌为服务端生成的安全随机串，Redis 中仅保存其 SHA-256 指纹
 * （键 {@code auth:refresh:<指纹>}，值为归属用户 ID），库泄露不泄露令牌本体。
 * 每次刷新执行旋转：作废旧令牌并签发新对；检测到已作废令牌被再次提交时，
 * 视为令牌泄露，吊销该用户全部刷新令牌并拒绝请求。
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    /** 刷新令牌有效期 */
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 为用户签发一个新刷新令牌。
     *
     * @param userId 归属用户 ID
     * @return 刷新令牌明文（仅在签发时返回一次）
     */
    public String issue(Long userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(fingerprintKey(token), String.valueOf(userId), REFRESH_TOKEN_TTL);
        return token;
    }

    /**
     * 校验刷新令牌并返回其归属用户 ID。
     *
     * @param token 客户端提交的刷新令牌
     * @return 归属用户 ID
     * @throws BusinessException 令牌不存在/已过期（1021/1020），或已被使用过（判定泄露，1022，
     *         并吊销该用户全部刷新令牌）
     */
    public Long validate(String token) {
        String fingerprint = fingerprint(token);
        Object usedBy = redisTemplate.opsForValue().get(RedisConstants.AUTH_REFRESH_TOKEN_USED_PREFIX + fingerprint);
        if (usedBy != null) {
            revokeAllForUser(Long.valueOf(String.valueOf(usedBy)));
            throw new BusinessException(AuthErrorConstants.REFRESH_TOKEN_REUSE, "刷新令牌已被使用，疑似泄露，已吊销全部会话");
        }
        Object owner = redisTemplate.opsForValue().get(RedisConstants.AUTH_REFRESH_TOKEN_PREFIX + fingerprint);
        if (owner == null) {
            throw new BusinessException(AuthErrorConstants.REFRESH_TOKEN_EXPIRED, "刷新令牌无效或已过期");
        }
        return Long.valueOf(String.valueOf(owner));
    }

    /**
     * 旋转：作废旧令牌（保留已用标记用于重放检测）并签发新令牌。
     *
     * @param oldToken 已通过校验的旧令牌
     * @param userId   归属用户 ID
     * @return 新刷新令牌明文
     */
    public String rotate(String oldToken, Long userId) {
        String fingerprint = fingerprint(oldToken);
        redisTemplate.delete(RedisConstants.AUTH_REFRESH_TOKEN_PREFIX + fingerprint);
        redisTemplate
                .opsForValue()
                .set(
                        RedisConstants.AUTH_REFRESH_TOKEN_USED_PREFIX + fingerprint,
                        String.valueOf(userId),
                        REFRESH_TOKEN_TTL);
        return issue(userId);
    }

    /**
     * 吊销该用户名下的全部刷新令牌（登出、改密、封禁时调用）。
     *
     * <p>指纹键不含用户 ID，需扫描比对归属；键空间规模为活跃会话数级别，SCAN 增量遍历避免阻塞。
     *
     * @param userId 归属用户 ID
     */
    public void revokeAllForUser(Long userId) {
        String expected = String.valueOf(userId);
        scanAndDelete(RedisConstants.AUTH_REFRESH_TOKEN_PREFIX + "*", expected);
        scanAndDelete(RedisConstants.AUTH_REFRESH_TOKEN_USED_PREFIX + "*", expected);
    }

    private void scanAndDelete(String pattern, String expectedValue) {
        ScanOptions options =
                ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                if (expectedValue.equals(
                        String.valueOf(redisTemplate.opsForValue().get(key)))) {
                    redisTemplate.delete(key);
                }
            }
        }
    }

    private String fingerprintKey(String token) {
        return RedisConstants.AUTH_REFRESH_TOKEN_PREFIX + fingerprint(token);
    }

    private String fingerprint(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest unavailable", e);
        }
    }
}
