package com.rauio.smartdangjian.server.ai.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "AI流式聊天响应")
public record AiChatResponse(
        @Schema(description = "双关语响应")
        String punnyResponse,

        @Schema(description = "会话id")
        String sessionId,

        @Schema(description = "ai的输出内容", example = "")
        String output
) {


}
