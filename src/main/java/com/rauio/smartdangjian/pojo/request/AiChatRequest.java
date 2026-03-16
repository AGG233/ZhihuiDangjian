package com.rauio.smartdangjian.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI聊天请求体")
public class AiChatRequest {

    @Schema(description = "会话ID，可选", example = "session-001")
    private String sessionId;

    @Schema(description = "聊天/提问内容", example = "请用一句话介绍党的性质")
    @NotBlank(message = "message不能为空")
    private String message;
}
