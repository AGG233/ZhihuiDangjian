package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.constants.RedisConstants;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    private final RedisProperties redisProperties = new RedisProperties();
    private final RedisConfig redisConfig = new RedisConfig(redisProperties);

    @Test
    @DisplayName("redisObjectMapper 不启用全局 default typing，避免反序列化任意类型")
    void shouldNotIncludeGlobalDefaultTyping() throws JsonProcessingException {
        ObjectMapper mapper = redisConfig.createObjectMapper();

        Map<String, Object> data = new HashMap<>();
        data.put("id", "10001");

        String json = mapper.writeValueAsString(data);
        assertThat(json).doesNotContain("java.util.HashMap");
        assertThat(json).doesNotContain("@class");
    }

    @Test
    @DisplayName("WRITE_ONLY 属性不会被序列化到 Redis（安全：不暴露 password/idCard 等字段）")
    void shouldNotSerializeWriteOnlyProperty() throws JsonProcessingException {
        ObjectMapper mapper = redisConfig.createObjectMapper();

        var obj = new WriteOnlyTestBean();
        obj.setName("visible");
        obj.setSecret("should-not-appear");

        String json = mapper.writeValueAsString(obj);
        assertThat(json).doesNotContain("secret");
        assertThat(json).contains("name");
    }

    @Test
    @DisplayName("缓存 TTL 按缓存名稳定抖动，避免同一时刻集中失效")
    void shouldUseDeterministicJitteredTtlByCacheName() {
        Duration userTtl = redisConfig.ttlForCache(RedisConstants.USER_VO_CACHE_PREFIX);
        Duration hotspotTtl = redisConfig.ttlForCache(RedisConstants.LEARNING_HOTSPOT_CACHE_PREFIX);

        assertThat(redisConfig.ttlForCache(RedisConstants.USER_VO_CACHE_PREFIX)).isEqualTo(userTtl);
        assertThat(userTtl).isGreaterThanOrEqualTo(RedisConfig.DEFAULT_CACHE_TTL);
        assertThat(userTtl).isLessThanOrEqualTo(RedisConfig.DEFAULT_CACHE_TTL.plusMinutes(10));
        assertThat(hotspotTtl).isNotEqualTo(userTtl);
    }

    @Test
    @DisplayName("RedisCacheManager 为主要缓存名配置独立 TTL")
    void shouldConfigureKnownCacheNamesIndividually() {
        Map<String, ?> configurations =
                redisConfig.initialCacheConfigurations(RedisCacheConfiguration.defaultCacheConfig());

        assertThat(configurations.keySet())
                .containsExactlyInAnyOrderElementsOf(Set.of(
                        RedisConstants.LEARNING_HOTSPOT_CACHE_PREFIX,
                        RedisConstants.USER_PROFILE_CACHE_PREFIX,
                        RedisConstants.AI_FAQ_CACHE_PREFIX,
                        RedisConstants.USER_VO_CACHE_PREFIX,
                        RedisConfig.RESOURCE_META_CACHE));
    }

    @Test
    @DisplayName("Redisson 空密码归一化为 null，避免无密码 Redis 收到 AUTH")
    void shouldNormalizeEmptyPasswordForRedissonConfig() {
        Config config = new Config().setPassword("");

        redisConfig.redissonEmptyPasswordCustomizer().customize(config);

        assertThat(config.getPassword()).isNull();
        assertThat(RedisConfig.normalizePassword("secret")).isEqualTo("secret");
    }

    public static class WriteOnlyTestBean {

        private String name;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String secret;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
}
