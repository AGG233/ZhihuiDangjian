package com.rauio.smartdangjian.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.constants.RedisConstants;

@AutoConfiguration
@EnableCaching
public class RedisConfig {

    public static ObjectMapper createCacheObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 缓存序列化必须携带类型信息：GenericJackson2JsonRedisSerializer 传入自定义
        // ObjectMapper 时不会自动启用 default typing，反序列化将退化为 LinkedHashMap，
        // 导致 @Cacheable 方法二次调用抛 ClassCastException。
        // 启用 default typing 时用白名单限定可反序列化类型，避免任意反序列化安全风险。
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.rauio.smartdangjian.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.lang.")
                .build();
        om.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return om;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(createCacheObjectMapper());

        template.setValueSerializer(jsonSerializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(createCacheObjectMapper());
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();

        // 按缓存名差异化 TTL（分级缓存策略）：热点/趋势数据时效性强用短 TTL，
        // 用户画像与用户资料允许分钟级滞后，其余沿用全局默认 1 小时
        java.util.Map<String, Duration> ttlByCacheName = new java.util.LinkedHashMap<>();
        ttlByCacheName.put("search:hot:courses:", Duration.ofMinutes(10));
        ttlByCacheName.put("search:hot:categories:", Duration.ofMinutes(10));
        ttlByCacheName.put("search:trend:learning:", Duration.ofMinutes(10));
        ttlByCacheName.put(RedisConstants.USER_PROFILE_CACHE_PREFIX, Duration.ofMinutes(30));
        ttlByCacheName.put(RedisConstants.USER_VO_CACHE_PREFIX, Duration.ofMinutes(30));

        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(defaultConfig);
        ttlByCacheName.forEach((name, ttl) -> builder.withCacheConfiguration(name, defaultConfig.entryTtl(ttl)));

        return builder.build();
    }
}
