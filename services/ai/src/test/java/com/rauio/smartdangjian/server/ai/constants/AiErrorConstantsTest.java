package com.rauio.smartdangjian.server.ai.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiErrorConstantsTest {

    @Test
    @DisplayName("AGENT_NOT_REGISTERED 常数值为 8001")
    void agentNotRegistered() {
        assertThat(AiErrorConstants.AGENT_NOT_REGISTERED).isEqualTo(8001);
    }

    @Test
    @DisplayName("SKILL_NOT_FOUND 常数值为 8002")
    void skillNotFound() {
        assertThat(AiErrorConstants.SKILL_NOT_FOUND).isEqualTo(8002);
    }

    @Test
    @DisplayName("SKILL_NOT_IN_CACHE 常数值为 8003")
    void skillNotInCache() {
        assertThat(AiErrorConstants.SKILL_NOT_IN_CACHE).isEqualTo(8003);
    }

    @Test
    @DisplayName("PROMPT_NOT_FOUND 常数值为 8004")
    void promptNotFound() {
        assertThat(AiErrorConstants.PROMPT_NOT_FOUND).isEqualTo(8004);
    }

    @Test
    @DisplayName("AI 模块错误码范围在 8000-8999 之间")
    void errorCodeRange() {
        assertThat(AiErrorConstants.AGENT_NOT_REGISTERED).isBetween(8000, 8999);
        assertThat(AiErrorConstants.SKILL_NOT_FOUND).isBetween(8000, 8999);
        assertThat(AiErrorConstants.SKILL_NOT_IN_CACHE).isBetween(8000, 8999);
        assertThat(AiErrorConstants.PROMPT_NOT_FOUND).isBetween(8000, 8999);
    }
}
