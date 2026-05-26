package com.rauio.smartdangjian.server.ai.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RedisConstantsTest {

    @Test
    @DisplayName("REDIS_AI_PROMPT 常数值为 ai_prompt:")
    void redisAiPrompt() {
        assertThat(RedisConstants.REDIS_AI_PROMPT).isEqualTo("ai_prompt:");
    }

    @Test
    @DisplayName("SYSTEM_PROMPT 常数值为 system:")
    void systemPrompt() {
        assertThat(RedisConstants.SYSTEM_PROMPT).isEqualTo("system:");
    }

    @Test
    @DisplayName("USER_PROMPT 常数值为 user:")
    void userPrompt() {
        assertThat(RedisConstants.USER_PROMPT).isEqualTo("user:");
    }

    @Test
    @DisplayName("private 构造器覆盖")
    void privateConstructor() throws Exception {
        Constructor<RedisConstants> constructor = RedisConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
