package com.rauio.smartdangjian.server.ai.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AiErrorConstants AI 模块错误码常量")
class AiErrorConstantsTest {

    @Test
    @DisplayName("错误码在 8000-8999 范围内")
    void errorCodesAreInRange() {
        assertThat(AiErrorConstants.AGENT_NOT_REGISTERED).isEqualTo(8001);
        assertThat(AiErrorConstants.SKILL_NOT_FOUND).isEqualTo(8002);
        assertThat(AiErrorConstants.SKILL_NOT_IN_CACHE).isEqualTo(8003);
        assertThat(AiErrorConstants.PROMPT_NOT_FOUND).isEqualTo(8004);

        assertThat(AiErrorConstants.AGENT_NOT_REGISTERED).isBetween(8000, 8999);
        assertThat(AiErrorConstants.SKILL_NOT_FOUND).isBetween(8000, 8999);
        assertThat(AiErrorConstants.SKILL_NOT_IN_CACHE).isBetween(8000, 8999);
        assertThat(AiErrorConstants.PROMPT_NOT_FOUND).isBetween(8000, 8999);
    }
}
