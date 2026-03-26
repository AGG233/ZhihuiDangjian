package com.rauio.smartdangjian.server.ai.tool.quiz;

import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import com.rauio.smartdangjian.server.ai.service.AiChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SaveQuizReasoningTool {

    private static final String META_KEY_QUIZ_REASONING = "quizReasoning";

    private final AiChatMessageService messageService;

    @Tool(name = "saveQuizReasoning", description = "保存出题思路到当前会话的meta信息中")
    public Boolean saveQuizReasoning(
            @ToolParam(description = "会话ID") String sessionId,
            @ToolParam(description = "出题思路内容") String reasoning
    ) {
        AiChatMessage message = messageService.get(sessionId);
        Map<String, Object> metadata = message.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(META_KEY_QUIZ_REASONING, reasoning);
        message.setMetadata(metadata);
        return messageService.update(message);
    }
}
