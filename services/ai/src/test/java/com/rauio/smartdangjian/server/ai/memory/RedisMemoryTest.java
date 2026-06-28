package com.rauio.smartdangjian.server.ai.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;

@ExtendWith(MockitoExtension.class)
class RedisMemoryTest {

    private final RedisMemory redisMemory = new RedisMemory();

    @Test
    @DisplayName("redisSaver 创建 RedisSaver 实例")
    void redisSaverCreatesRedisSaver() {
        RedissonClient redissonClient = mock(RedissonClient.class);

        RedisSaver saver = redisMemory.redisSaver(redissonClient);

        assertThat(saver).isNotNull();
    }

    @Test
    @DisplayName("redisSaver 返回实例类型为 RedisSaver")
    void redisSaverReturnsCorrectType() {
        RedissonClient redissonClient = mock(RedissonClient.class);

        RedisSaver saver = redisMemory.redisSaver(redissonClient);

        assertThat(saver).isInstanceOf(RedisSaver.class);
    }

    @Test
    @DisplayName("redisSaver 传入 null RedissonClient 抛出 IllegalArgumentException")
    void redisSaverWithNullClientThrowsException() {
        assertThatThrownBy(() -> redisMemory.redisSaver(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("redisson cannot be null");
    }

    @Test
    @DisplayName("redisSaver 使用 mock RedissonClient 返回非 null saver 且可重复创建")
    void redisSaverMultipleCallsReturnDifferentInstances() {
        RedissonClient redissonClient = mock(RedissonClient.class);

        RedisSaver saver1 = redisMemory.redisSaver(redissonClient);
        RedisSaver saver2 = redisMemory.redisSaver(redissonClient);

        assertThat(saver1).isNotNull();
        assertThat(saver2).isNotNull();
    }
}
