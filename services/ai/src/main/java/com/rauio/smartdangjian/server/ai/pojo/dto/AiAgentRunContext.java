package com.rauio.smartdangjian.server.ai.pojo.dto;

import com.rauio.smartdangjian.server.ai.agent.AiAgentType;

public record AiAgentRunContext(
        AiAgentType agentType,
        String sessionId,
        String userId,
        String input
) {
}
