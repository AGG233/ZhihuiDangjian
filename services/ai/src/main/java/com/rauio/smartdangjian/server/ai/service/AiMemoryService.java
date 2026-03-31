package com.rauio.smartdangjian.server.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.rauio.smartdangjian.server.ai.constants.AiConstants.MESSAGE_INPUT;
import static com.rauio.smartdangjian.server.ai.constants.AiConstants.MESSAGE_OUTPUT;
import static com.rauio.smartdangjian.server.ai.constants.AiConstants.SENDER_ASSISTANT;
import static com.rauio.smartdangjian.server.ai.constants.AiConstants.SENDER_USER;

@Service
@RequiredArgsConstructor
@Transactional
public class AiMemoryService {

    private final AiChatMessageService aiChatMessageService;

    public void saveConversation(String userId, String sessionId, String agentType, String input, String output) {
        if (userId == null || sessionId == null) {
            return;
        }
        aiChatMessageService.save(buildMessage(userId, sessionId, agentType, SENDER_USER, MESSAGE_INPUT, input, Map.of()));
        aiChatMessageService.save(buildMessage(userId, sessionId, agentType, SENDER_ASSISTANT, MESSAGE_OUTPUT, output, Map.of()));
    }

    public String buildLongTermMemory(String userId, String sessionId, int limit) {
        if (userId == null || userId.isBlank()) {
            return "";
        }
        List<AiChatMessage> memories = aiChatMessageService.list(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getUserId, userId)
                .ne(sessionId != null && !sessionId.isBlank(), AiChatMessage::getSessionId, sessionId)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .last("limit " + Math.max(limit, 1)));

        if (memories.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = memories.size() - 1; i >= 0; i--) {
            AiChatMessage message = memories.get(i);
            builder.append("- [")
                    .append(message.getAgentType())
                    .append('/')
                    .append(message.getSenderType())
                    .append("] ")
                    .append(message.getContent())
                    .append('\n');
        }
        return builder.toString().trim();
    }

    public List<AiChatMessage> listSessionMessages(String userId, String sessionId) {
        return aiChatMessageService.list(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getUserId, userId)
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getCreatedAt));
    }

    private AiChatMessage buildMessage(String userId,
                                       String sessionId,
                                       String agentType,
                                       String senderType,
                                       String messageType,
                                       String content,
                                       Map<String, Object> metadata) {
        return AiChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .sessionId(sessionId)
                .agentType(agentType)
                .senderType(senderType)
                .messageType(messageType)
                .content(content)
                .metadata(new HashMap<>(metadata))
                .build();
    }
}
