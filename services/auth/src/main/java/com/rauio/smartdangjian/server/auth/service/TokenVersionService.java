package com.rauio.smartdangjian.server.auth.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.constants.RedisConstants;

import lombok.RequiredArgsConstructor;

/**
 * 访问令牌版本号服务（无状态 JWT 的吊销补偿机制）。
 *
 * <p>无状态 JWT 一旦签发即无法在服务端单独吊销；将当前版本号写入 Redis，
 * 并在签发时把版本号编入 JWT claim（{@code ver}）。鉴权侧比对 claim 与 Redis
 * 当前值，不一致即拒绝——改密、封禁、注销等场景调用 {@link #bump} 即可使该用户
 * 全部已签发访问令牌立即失效，等效替代 Stateless 模式下不可用的踢人下线能力。
 */
@Service
@RequiredArgsConstructor
public class TokenVersionService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 读取用户当前令牌版本号；从未写入过时视为 0（与登录时惰性初始化一致）。
     *
     * @param userId 用户 ID
     * @return 当前版本号（未初始化时为 0）
     */
    public long current(Long userId) {
        Object version = redisTemplate.opsForValue().get(RedisConstants.AUTH_TOKEN_VERSION_PREFIX + userId);
        return version instanceof Number n ? n.longValue() : 0L;
    }

    /**
     * 版本号自增，使用户全部存量访问令牌立即失效。
     *
     * @param userId 用户 ID
     * @return 自增后的新版本号
     */
    public long bump(Long userId) {
        Long version = redisTemplate.opsForValue().increment(RedisConstants.AUTH_TOKEN_VERSION_PREFIX + userId);
        return version != null ? version : 1L;
    }
}
