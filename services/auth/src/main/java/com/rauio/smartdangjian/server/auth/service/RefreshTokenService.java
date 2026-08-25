package com.rauio.smartdangjian.server.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.constants.RedisConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;

import lombok.RequiredArgsConstructor;

/**
 * 刷新令牌服务（无状态 JWT 双令牌体系的可吊销侧）。
 *
 * <p>刷新令牌为服务端生成的安全随机串，Redis 中仅保存其 SHA-256 指纹
 * （键 {@code auth:refresh:<指纹>}，值为 {@code 用户ID:平台}），库泄露不泄露令牌本体。
 * 每次刷新以 Lua 脚本原子完成「校验并作废旧令牌 + 写入已用标记」——校验与作废之间
 * 不存在竞态窗口，并发提交同一令牌时仅一个请求能成功；检测到已作废令牌被再次提交时，
 * 视为令牌泄露，吊销该用户全部刷新令牌并拒绝请求。
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    /** 刷新令牌有效期 */
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private static final String VALUE_SEPARATOR = ":";

    /** 原子消费成功状态 */
    private static final String STATUS_OK = "OK";

    /** 原子消费重放状态 */
    private static final String STATUS_REUSED = "REUSED";

    /**
     * 原子消费脚本：GET 旧令牌归属值 → DEL 旧键 → SET 已用标记（沿用原 TTL 语义）。
     * 返回 {@code [OK, 归属值]} / {@code [REUSED, 已用归属值]} / {@code [EXPIRED, '']}。
     */
    private static final RedisScript<List> CONSUME_SCRIPT = new DefaultRedisScript<>(
            String.join(
                    "\n",
                    "local owner = redis.call('GET', KEYS[1])",
                    "if owner then",
                    "  redis.call('DEL', KEYS[1])",
                    "  redis.call('SET', KEYS[2], owner, 'EX', tonumber(ARGV[1]))",
                    "  return {'" + STATUS_OK + "', owner}",
                    "end",
                    "local usedBy = redis.call('GET', KEYS[2])",
                    "if usedBy then",
                    "  return {'" + STATUS_REUSED + "', usedBy}",
                    "end",
                    "return {'EXPIRED', ''}"),
            List.class);

    private final RedisTemplate<String, Object> redisTemplate;

    /** 刷新令牌归属信息：用户 ID 与签发平台 */
    public record TokenIdentity(Long userId, String platform) {}

    /**
     * 为用户签发一个新刷新令牌。
     *
     * @param userId   归属用户 ID
     * @param platform 签发平台（web/app），刷新时按原平台续签访问令牌有效期
     * @return 刷新令牌明文（仅在签发时返回一次）
     */
    public String issue(Long userId, String platform) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(fingerprintKey(token), encodeOwner(userId, platform), REFRESH_TOKEN_TTL);
        return token;
    }

    /**
     * 原子消费刷新令牌：校验、作废与已用标记写入在同一个 Redis 脚本内完成，
     * 并发提交同一令牌时仅有一个请求能成功，其余按重放处理。
     *
     * @param token 客户端提交的刷新令牌
     * @return 归属信息（用户 ID + 签发平台）
     * @throws BusinessException 令牌不存在/已过期（1021），或已被使用过（判定泄露，1022，
     *         并吊销该用户全部刷新令牌）
     */
    public TokenIdentity consume(String token) {
        String fingerprint = fingerprint(token);
        List<?> result = redisTemplate.execute(
                CONSUME_SCRIPT,
                List.of(
                        RedisConstants.AUTH_REFRESH_TOKEN_PREFIX + fingerprint,
                        RedisConstants.AUTH_REFRESH_TOKEN_USED_PREFIX + fingerprint),
                REFRESH_TOKEN_TTL.toSeconds());
        String status = result == null || result.isEmpty() ? "" : String.valueOf(result.get(0));
        String owner =
                result == null || result.size() < 2 || result.get(1) == null ? "" : String.valueOf(result.get(1));
        if (STATUS_OK.equals(status)) {
            return decodeOwner(owner);
        }
        if (STATUS_REUSED.equals(status)) {
            Long replayUserId = extractUserId(owner);
            revokeAllForUser(replayUserId);
            throw new BusinessException(AuthErrorConstants.REFRESH_TOKEN_REUSE, "刷新令牌已被使用，疑似泄露，已吊销全部会话");
        }
        throw new BusinessException(AuthErrorConstants.REFRESH_TOKEN_EXPIRED, "刷新令牌无效或已过期");
    }

    /**
     * 吊销该用户名下的全部刷新令牌（登出、改密、封禁/停用时调用）。
     *
     * <p>指纹键不含用户 ID，需扫描比对归属；键空间规模为活跃会话数级别，SCAN 增量遍历避免阻塞。
     *
     * @param userId 归属用户 ID
     */
    public void revokeAllForUser(Long userId) {
        scanAndDelete(RedisConstants.AUTH_REFRESH_TOKEN_PREFIX + "*", userId);
        scanAndDelete(RedisConstants.AUTH_REFRESH_TOKEN_USED_PREFIX + "*", userId);
    }

    private void scanAndDelete(String pattern, Long userId) {
        ScanOptions options =
                ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                if (ownedBy(redisTemplate.opsForValue().get(key), userId)) {
                    redisTemplate.delete(key);
                }
            }
        }
    }

    /** 归属值格式为 {@code userId:platform}；无分隔符的历史值按纯 userId 兼容匹配。 */
    private boolean ownedBy(Object value, Long userId) {
        if (value == null) {
            return false;
        }
        String owner = String.valueOf(value);
        int sep = owner.indexOf(VALUE_SEPARATOR);
        String idPart = sep <= 0 ? owner : owner.substring(0, sep);
        return String.valueOf(userId).equals(idPart);
    }

    private String encodeOwner(Long userId, String platform) {
        return userId + VALUE_SEPARATOR + (platform == null ? "web" : platform);
    }

    private TokenIdentity decodeOwner(String owner) {
        int sep = owner.indexOf(VALUE_SEPARATOR);
        if (sep <= 0) {
            // 历史纯 userId 值兼容：平台缺省按 web 处理
            return new TokenIdentity(Long.valueOf(owner), "web");
        }
        return new TokenIdentity(Long.valueOf(owner.substring(0, sep)), owner.substring(sep + 1));
    }

    private Long extractUserId(String owner) {
        int sep = owner.indexOf(VALUE_SEPARATOR);
        return Long.valueOf(sep <= 0 ? owner : owner.substring(0, sep));
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
