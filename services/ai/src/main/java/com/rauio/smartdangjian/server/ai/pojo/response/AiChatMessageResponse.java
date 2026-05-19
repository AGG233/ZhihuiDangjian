package com.rauio.smartdangjian.server.ai.pojo.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI聊天消息响应")
public class AiChatMessageResponse {

    @Schema(description = "消息ID")
    private String id;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "Agent类型", example = "CHAT")
    private String agentType;

    @Schema(description = "发送者类型", example = "USER")
    private String senderType;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型", example = "TEXT")
    private String messageType;

    @Schema(description = "扩展元数据")
    private Map<String, Object> metadata;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static AiChatMessageResponse fromEntity(AiChatMessage entity) {
        if (entity == null) {
            return null;
        }
        return AiChatMessageResponse.builder()
                .id(entity.getId())
                .sessionId(entity.getSessionId())
                .userId(entity.getUserId())
                .agentType(entity.getAgentType())
                .senderType(entity.getSenderType())
                .content(entity.getContent())
                .messageType(entity.getMessageType())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
