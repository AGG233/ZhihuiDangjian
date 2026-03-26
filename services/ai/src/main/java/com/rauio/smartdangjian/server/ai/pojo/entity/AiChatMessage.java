package com.rauio.smartdangjian.server.ai.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@TableName("ai_chat_message")
public class AiChatMessage {

    @TableId
    private String id;

    @TableField(fill = FieldFill.INSERT)
    private String sessionId;

    private String userId;

    private String senderType;

    private String content;

    private String messageType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}
