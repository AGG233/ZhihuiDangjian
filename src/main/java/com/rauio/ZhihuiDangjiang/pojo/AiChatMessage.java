package com.rauio.ZhihuiDangjiang.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@TableName("ai_chat_message")
public class AiChatMessage {

    @TableId
    private Long id;

    private String sessionId;

    private Long userId;

    private String senderType;

    private String content;

    private String messageType;

    private String metadata;

    private Date createdAt;

}
