package com.rauio.smartdangjian.server.ai.memory;

import static org.assertj.core.api.Assertions.assertThat;
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
}
