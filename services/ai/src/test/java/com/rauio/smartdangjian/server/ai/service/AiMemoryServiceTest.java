package com.rauio.smartdangjian.server.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;

@ExtendWith(MockitoExtension.class)
class AiMemoryServiceTest {

    @Mock
    private AiChatMessageService aiChatMessageService;

    @InjectMocks
    private AiMemoryService aiMemoryService;

    @Captor
    private ArgumentCaptor<AiChatMessage> messageCaptor;

    @Test
    @DisplayName("saveConversation 保存用户消息和 AI 回复")
    void saveConversation() {
        doReturn(true).when(aiChatMessageService).save(any(AiChatMessage.class));

        aiMemoryService.saveConversation("user-1", "session-1", "CHAT", "你好", "你好，有什么可以帮助你的？");

        verify(aiChatMessageService, times(2)).save(messageCaptor.capture());
        List<AiChatMessage> messages = messageCaptor.getAllValues();

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getUserId()).isEqualTo("user-1");
        assertThat(messages.get(0).getSessionId()).isEqualTo("session-1");
        assertThat(messages.get(0).getSenderType()).isEqualTo("user");
        assertThat(messages.get(0).getMessageType()).isEqualTo("text");
        assertThat(messages.get(0).getContent()).isEqualTo("你好");

        assertThat(messages.get(1).getUserId()).isEqualTo("user-1");
        assertThat(messages.get(1).getSessionId()).isEqualTo("session-1");
        assertThat(messages.get(1).getSenderType()).isEqualTo("ai");
        assertThat(messages.get(1).getContent()).isEqualTo("你好，有什么可以帮助你的？");
    }

    @Test
    @DisplayName("saveConversation 用户 ID 为空时直接返回")
    void saveConversationNullUserId() {
        aiMemoryService.saveConversation(null, "session-1", "CHAT", "你好", "回复");

        verify(aiChatMessageService, never()).save(any());
    }

    @Test
    @DisplayName("saveConversation 会话 ID 为空时直接返回")
    void saveConversationBlankSessionId() {
        aiMemoryService.saveConversation("user-1", "", "CHAT", "你好", "回复");

        verify(aiChatMessageService, never()).save(any());
    }

    @Test
    @DisplayName("saveConversation AI 输出为空或空白时使用默认文本")
    void saveConversationBlankOutput() {
        doReturn(true).when(aiChatMessageService).save(any(AiChatMessage.class));

        aiMemoryService.saveConversation("user-1", "session-1", "CHAT", "你好", "");

        verify(aiChatMessageService, times(2)).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("[AI 未返回文本内容]");
    }

    @Test
    @DisplayName("buildLongTermMemory 构建长期记忆字符串")
    void buildLongTermMemory() {
        AiChatMessage msg1 = AiChatMessage.builder()
                .agentType("CHAT")
                .senderType("user")
                .content("用户提问")
                .build();
        AiChatMessage msg2 = AiChatMessage.builder()
                .agentType("CHAT")
                .senderType("ai")
                .content("AI回复")
                .build();

        doReturn(List.of(msg1, msg2)).when(aiChatMessageService).list(any(LambdaQueryWrapper.class));

        String memory = aiMemoryService.buildLongTermMemory("user-1", "session-1", 10);

        assertThat(memory).contains("[CHAT/user] 用户提问");
        assertThat(memory).contains("[CHAT/ai] AI回复");
    }

    @Test
    @DisplayName("buildLongTermMemory userId 为 null 时返回空字符串")
    void buildLongTermMemoryNullUserId() {
        String memory = aiMemoryService.buildLongTermMemory(null, "session-1", 10);

        assertThat(memory).isEmpty();
        verify(aiChatMessageService, never()).list(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("buildLongTermMemory userId 为空白时返回空字符串")
    void buildLongTermMemoryBlankUserId() {
        String memory = aiMemoryService.buildLongTermMemory("", "session-1", 10);

        assertThat(memory).isEmpty();
    }

    @Test
    @DisplayName("buildLongTermMemory 无历史消息时返回空字符串")
    void buildLongTermMemoryNoHistory() {
        doReturn(List.of()).when(aiChatMessageService).list(any(LambdaQueryWrapper.class));

        String memory = aiMemoryService.buildLongTermMemory("user-1", "session-1", 10);

        assertThat(memory).isEmpty();
    }

    @Test
    @DisplayName("listSessionMessages 按创建时间升序返回会话消息")
    void listSessionMessages() {
        AiChatMessage msg1 = AiChatMessage.builder().content("msg1").build();
        AiChatMessage msg2 = AiChatMessage.builder().content("msg2").build();
        doReturn(List.of(msg1, msg2)).when(aiChatMessageService).list(any(LambdaQueryWrapper.class));

        List<AiChatMessage> messages = aiMemoryService.listSessionMessages("user-1", "session-1");

        assertThat(messages).hasSize(2);
    }
}
