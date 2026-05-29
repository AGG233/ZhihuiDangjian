package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    @DisplayName("redisObjectMapper 序列化包含 @class 多态类型信息")
    void shouldIncludeTypeInfo() throws JsonProcessingException {
        ObjectMapper mapper = redisConfig.redisObjectMapper();

        Map<String, Object> data = new HashMap<>();
        data.put("id", "10001");

        String json = mapper.writeValueAsString(data);
        assertThat(json).contains("java.util.HashMap");
    }
}
