package com.rauio.smartdangjian.server.ai.memory;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;

@Configuration
public class RedisMemory {

    @Bean
    public RedisSaver redisSaver(RedissonClient redissonClient) {
        return RedisSaver.builder().redisson(redissonClient).build();
    }
}
