package com.rauio.smartdangjian.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI流式聊天响应")
public class AiChatStreamResponse {

    @Schema(description = "会话ID", example = "session-001")
    private String sessionId;

    @Schema(description = "本次增量内容", example = "党的性质是")
    private String content;

    @Schema(description = "内容生成时间")
    private LocalDateTime createdAt;

    public AiChatStreamResponse(String content) {
        this.content = content;
    }
}
