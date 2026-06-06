package com.rauio.smartdangjian.server.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import com.rauio.smartdangjian.server.ai.service.AiChatMessageService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuizTool {

    private final AiChatMessageService messageService;
    private final CurrentUserProvider currentUserProvider;

    @Tool(name = "getQuizReasoning", description = "获取当前会话中保存的出题思路")
    public Object getQuizReasoning(@ToolParam(description = "会话ID") String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        String userId = ToolContextUtil.resolveUserId(currentUserProvider);
        AiChatMessage message = messageService.findLatestBySessionIdAndUserId(sessionId, Long.valueOf(userId));
        return message == null ? null : message.getMetadata();
    }
}
