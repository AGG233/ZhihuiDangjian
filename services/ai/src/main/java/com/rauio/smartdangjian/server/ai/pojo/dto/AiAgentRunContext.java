package com.rauio.smartdangjian.server.ai.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI Agent运行上下文。当前保留用于扩展，路由决策由 Coordinator 自动处理。
 */
@Schema(description = "AI Agent运行上下文")
public record AiAgentRunContext(
        @Schema(description = "会话ID")
        String sessionId,
        @Schema(description = "用户ID")
        String userId,
        @Schema(description = "本次输入内容")
        String input
) {
}
