package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    @DisplayName("redisObjectMapper 包含 @class 多态类型信息（启用 activateDefaultTyping 修复缓存反序列化 ClassCastException）")
    void shouldIncludeTypeInfoForPolymorphicDeserialization() throws JsonProcessingException {
        ObjectMapper mapper = redisConfig.createObjectMapper();

        Map<String, Object> data = new HashMap<>();
        data.put("id", "10001");

        String json = mapper.writeValueAsString(data);
        assertThat(json).contains("java.util.HashMap");
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
