package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import com.rauio.smartdangjian.server.ai.service.AiChatMessageService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizTool {

    private final AiChatMessageService messageService;

    @Tool(name = "getQuizReasoning", description = "获取当前会话中保存的出题思路")
    public Object getQuizReasoning(@ToolParam(description = "会话ID，可为空") String sessionId, ToolContext toolContext) {
        String targetSessionId = (sessionId == null || sessionId.isBlank())
                ? ToolContextUtil.getSessionId(toolContext)
                : sessionId;
        AiChatMessage message = messageService.lambdaQuery()
                .eq(AiChatMessage::getSessionId, targetSessionId)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .last("limit 1")
                .one();
        return message == null ? null : message.getMetadata();
    }
}
