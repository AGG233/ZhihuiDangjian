package com.rauio.smartdangjian.config;

import java.time.Duration;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.constants.RedisConstants;

@AutoConfiguration
@EnableCaching
@AutoConfigureBefore(RedissonAutoConfigurationV2.class)
public class RedisConfig {

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * Redisson treats non-null password (including empty string) as a valid credential and sends AUTH.
     * When REDIS_PASSWORD is unset, ${REDIS_PASSWORD:} resolves to empty string, not null.
     * Normalize empty password to null so Redisson skips AUTH for passwordless Redis.
     */
    @PostConstruct
    void normalizeEmptyPassword() {
        String password = redisProperties.getPassword();
        if (password != null && password.isEmpty()) {
            redisProperties.setPassword(null);
        }
    }

    static final Duration DEFAULT_CACHE_TTL = Duration.ofHours(1);
    private static final Duration CACHE_TTL_JITTER_RANGE = Duration.ofMinutes(10);
    static final String RESOURCE_META_CACHE = "resourceMeta";

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.database}")
    private int database;

    private final ObjectMapper redisObjectMapper = createObjectMapper();

    ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return om;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);
        return template;
    }

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        RedisCacheConfiguration defaultConfig = defaultCacheConfiguration(keySerializer, valueSerializer);

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(initialCacheConfigurations(defaultConfig))
                .build();
    }

    RedisCacheConfiguration defaultCacheConfiguration(
            StringRedisSerializer keySerializer, GenericJackson2JsonRedisSerializer valueSerializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_CACHE_TTL)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();
    }

    Map<String, RedisCacheConfiguration> initialCacheConfigurations(RedisCacheConfiguration defaultConfig) {
        return Map.of(
                RedisConstants.LEARNING_HOTSPOT_CACHE_PREFIX,
                defaultConfig.entryTtl(ttlForCache(RedisConstants.LEARNING_HOTSPOT_CACHE_PREFIX)),
                RedisConstants.USER_PROFILE_CACHE_PREFIX,
                defaultConfig.entryTtl(ttlForCache(RedisConstants.USER_PROFILE_CACHE_PREFIX)),
                RedisConstants.AI_FAQ_CACHE_PREFIX,
                defaultConfig.entryTtl(ttlForCache(RedisConstants.AI_FAQ_CACHE_PREFIX)),
                RedisConstants.USER_VO_CACHE_PREFIX,
                defaultConfig.entryTtl(ttlForCache(RedisConstants.USER_VO_CACHE_PREFIX)),
                RESOURCE_META_CACHE,
                defaultConfig.entryTtl(ttlForCache(RESOURCE_META_CACHE)));
    }

    Duration ttlForCache(String cacheName) {
        long jitterSeconds = Math.floorMod(cacheName.hashCode(), CACHE_TTL_JITTER_RANGE.toSeconds() + 1);
        return DEFAULT_CACHE_TTL.plusSeconds(jitterSeconds);
    }
}
