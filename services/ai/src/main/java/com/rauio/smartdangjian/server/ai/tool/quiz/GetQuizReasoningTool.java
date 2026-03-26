package com.rauio.smartdangjian.server.ai.tool.quiz;

import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import com.rauio.smartdangjian.server.ai.service.AiChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetQuizReasoningTool {

    private static final String META_KEY_QUIZ_REASONING = "quizReasoning";

    private final AiChatMessageService messageService;

    @Tool(name = "getQuizReasoning", description = "获取当前会话中保存的出题思路")
    public Object getQuizReasoning(@ToolParam(description = "会话ID") String sessionId) {
        AiChatMessage message = messageService.get(sessionId);
        Map<String, Object> metadata = message.getMetadata();
        if (metadata == null) {
            return null;
        }
        return metadata.get(META_KEY_QUIZ_REASONING);
    }
}
