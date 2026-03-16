package com.rauio.smartdangjian.service.ai.tool;

import com.rauio.smartdangjian.pojo.AiChatMessage;
import com.rauio.smartdangjian.service.ai.AiChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizTool {

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
