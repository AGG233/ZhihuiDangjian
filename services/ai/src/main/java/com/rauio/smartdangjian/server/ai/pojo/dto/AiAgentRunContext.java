package com.rauio.smartdangjian.server.ai.pojo.dto;

import com.rauio.smartdangjian.server.ai.agent.AiAgentType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI Agent运行上下文")
public record AiAgentRunContext(
        @Schema(description = "Agent类型")
        AiAgentType agentType,
        @Schema(description = "会话ID")
        String sessionId,
        @Schema(description = "用户ID")
        String userId,
        @Schema(description = "本次输入内容")
        String input
) {
}
