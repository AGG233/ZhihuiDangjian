package com.rauio.smartdangjian.server.ai.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "ai_chat_message", autoResultMap = true)
@Schema(description = "AI聊天消息")
public class AiChatMessage {

    @TableId
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

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "扩展元数据")
    private Map<String, Object> metadata;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}
