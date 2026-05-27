package com.rauio.smartdangjian.server.ai.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RedisConstants AI 模块 Redis Key 常量")
class RedisConstantsTest {

    @Test
    @DisplayName("常量值正确")
    void constants() {
        assertThat(RedisConstants.REDIS_AI_PROMPT).isEqualTo("ai_prompt:");
        assertThat(RedisConstants.SYSTEM_PROMPT).isEqualTo("system:");
        assertThat(RedisConstants.USER_PROMPT).isEqualTo("user:");
    }
}
