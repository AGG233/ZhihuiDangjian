package com.rauio.smartdangjian.server.ai.pojo.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiAgentRunContextTest {

    @Test
    @DisplayName("创建 AiAgentRunContext 记录并验证字段值")
    void createAndVerifyFields() {
        AiAgentRunContext context = new AiAgentRunContext("session-1", "user-1", "你好");

        assertThat(context.sessionId()).isEqualTo("session-1");
        assertThat(context.userId()).isEqualTo("user-1");
        assertThat(context.input()).isEqualTo("你好");
    }

    @Test
    @DisplayName("创建带 null 字段的 AiAgentRunContext")
    void createWithNullFields() {
        AiAgentRunContext context = new AiAgentRunContext(null, null, null);

        assertThat(context.sessionId()).isNull();
        assertThat(context.userId()).isNull();
        assertThat(context.input()).isNull();
    }

    @Test
    @DisplayName("AiAgentRunContext 记录相等性")
    void recordEquality() {
        AiAgentRunContext ctx1 = new AiAgentRunContext("s1", "u1", "hello");
        AiAgentRunContext ctx2 = new AiAgentRunContext("s1", "u1", "hello");

        assertThat(ctx1).isEqualTo(ctx2);
        assertThat(ctx1.hashCode()).isEqualTo(ctx2.hashCode());
    }

    @Test
    @DisplayName("AiAgentRunContext 记录 toString 包含字段内容")
    void recordToString() {
        AiAgentRunContext context = new AiAgentRunContext("session-1", "user-1", "你好");

        assertThat(context.toString())
                .contains("session-1")
                .contains("user-1")
                .contains("你好");
    }
}
