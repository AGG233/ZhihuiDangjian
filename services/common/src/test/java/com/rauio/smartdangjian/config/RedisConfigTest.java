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
    @DisplayName("redisObjectMapper 序列化包含 @class 多态类型信息")
    void shouldIncludeTypeInfo() throws JsonProcessingException {
        ObjectMapper mapper = redisConfig.createObjectMapper();

        Map<String, Object> data = new HashMap<>();
        data.put("id", "10001");

        String json = mapper.writeValueAsString(data);
        assertThat(json).contains("java.util.HashMap");
    }

    @Test
    @DisplayName("WRITE_ONLY 属性在自定义 ObjectMapper 中也会被序列化（被重写为 READ_WRITE）")
    void shouldSerializeWriteOnlyPropertyAsReadWrite() throws JsonProcessingException {
        ObjectMapper mapper = redisConfig.createObjectMapper();

        var obj = new WriteOnlyTestBean();
        obj.setName("visible");
        obj.setSecret("should-appear");

        String json = mapper.writeValueAsString(obj);
        // 验证 secret 出现在 JSON 中（说明 WRITE_ONLY 被覆盖为 READ_WRITE）
        assertThat(json).contains("secret");
        assertThat(json).contains("should-appear");
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
