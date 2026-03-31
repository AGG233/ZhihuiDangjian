package com.rauio.smartdangjian.server.ai.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "AI流式聊天响应")
public record AiChatResponse(
        @Schema(description = "事件类型", example = "THINKING")
        String type,

        @Schema(description = "会话id")
        String sessionId,

        @Schema(description = "ai的输出内容")
        String output,

        @Schema(description = "当前输出节点", example = "chat-agent")
        String node,

        @Schema(description = "当前使用的 Agent", example = "chat-agent")
        String agent
) {


}
