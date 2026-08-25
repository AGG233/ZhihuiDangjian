package com.rauio.smartdangjian.server.ai.config;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * AI 接口分布式限流器。
 *
 * <p>基于 Redisson {@link RRateLimiter}（OVERALL 模式）在 Redis 侧统一计数，
 * 多实例部署时限流额度全局共享——替代历史 JVM 内存版（多实例下各自独立计数导致限流失效）。
 */
@Component
@RequiredArgsConstructor
public class AiRateLimiter {

    private static final String KEY_PREFIX = "ratelimit:ai:";

    private final RedissonClient redissonClient;

    @Value("${ai.rate-limit.requests-per-minute:10}")
    private int requestsPerMinute;

    /**
     * 尝试为指定主体（用户 ID 或回退 IP）获取一次调用配额。
     *
     * @param subject 限流主体标识
     * @return true 表示放行；false 表示超出每分钟额度
     */
    public boolean tryAcquire(String subject) {
        RRateLimiter limiter = redissonClient.getRateLimiter(KEY_PREFIX + subject);
        limiter.setRate(RateType.OVERALL, requestsPerMinute, 1, RateIntervalUnit.MINUTES);
        return limiter.tryAcquire();
    }
}
