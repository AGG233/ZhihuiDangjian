package com.rauio.smartdangjian.server.ai.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "AI聊天请求体")
public record AiChatRequest(

        @Schema(description = "会话ID，可选")
        String sessionId,

        @Schema(description = "聊天/提问内容", example = "请用一句话介绍党的性质")
        @NotBlank(message = "message不能为空")
        String message
) {

}
