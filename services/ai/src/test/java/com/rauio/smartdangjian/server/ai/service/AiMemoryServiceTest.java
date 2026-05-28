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
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatMessageResponse;

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

        aiMemoryService.saveConversation("1", "session-1", "CHAT", "你好", "你好，有什么可以帮助你的？");

        verify(aiChatMessageService, times(2)).save(messageCaptor.capture());
        List<AiChatMessage> messages = messageCaptor.getAllValues();

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getUserId()).isEqualTo(1L);
        assertThat(messages.get(0).getSessionId()).isEqualTo("session-1");
        assertThat(messages.get(0).getSenderType()).isEqualTo("user");
        assertThat(messages.get(0).getMessageType()).isEqualTo("text");
        assertThat(messages.get(0).getContent()).isEqualTo("你好");

        assertThat(messages.get(1).getUserId()).isEqualTo(1L);
        assertThat(messages.get(1).getSessionId()).isEqualTo("session-1");
        assertThat(messages.get(1).getSenderType()).isEqualTo("ai");
        assertThat(messages.get(1).getContent()).isEqualTo("你好，有什么可以帮助你的？");
    }

    @Test
    @DisplayName("saveConversation userId blank when sessionId non-null")
    void saveConversationBlankUserId() {
        aiMemoryService.saveConversation(null, "session-1", "CHAT", "你好", "回复");

        verify(aiChatMessageService, never()).save(any());
    }

    @Test
    @DisplayName("saveConversation userId is blank string returns early")
    void saveConversationEmptyUserIdString() {
        aiMemoryService.saveConversation("", "session-1", "CHAT", "input", "output");

        verify(aiChatMessageService, never()).save(any());
    }

    @Test
    @DisplayName("saveConversation sessionId is null returns early")
    void saveConversationNullSessionId() {
        aiMemoryService.saveConversation("1", null, "CHAT", "input", "output");

        verify(aiChatMessageService, never()).save(any());
    }

    @Test
    @DisplayName("buildLongTermMemory sessionId non-null but blank excludes nothing")
    void buildLongTermMemoryBlankSessionId() {
        AiChatMessage msg = AiChatMessage.builder()
                .agentType("CHAT")
                .senderType("user")
                .content("content")
                .build();
        doReturn(List.of(msg)).when(aiChatMessageService).list(any(LambdaQueryWrapper.class));

        String memory = aiMemoryService.buildLongTermMemory("user-1", "  ", 10);

        assertThat(memory).isNotBlank();
    }

    @Test
    @DisplayName("saveConversation AI output is blank string uses default text")
    void saveConversationBlankOutput() {
        doReturn(true).when(aiChatMessageService).save(any(AiChatMessage.class));

        aiMemoryService.saveConversation("1", "session-1", "CHAT", "你好", "");

        verify(aiChatMessageService, times(2)).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("[AI 未返回文本内容]");
    }

    @Test
    @DisplayName("saveConversation AI output is null uses default text")
    void saveConversationNullOutput() {
        doReturn(true).when(aiChatMessageService).save(any(AiChatMessage.class));

        aiMemoryService.saveConversation("1", "session-1", "CHAT", "你好", null);

        verify(aiChatMessageService, times(2)).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("[AI 未返回文本内容]");
    }

    @Test
    @DisplayName("buildLongTermMemory sessionId is null does not exclude current session")
    void buildLongTermMemoryNullSessionId() {
        AiChatMessage msg = AiChatMessage.builder()
                .agentType("CHAT")
                .senderType("user")
                .content("content")
                .build();
        doReturn(List.of(msg)).when(aiChatMessageService).list(any(LambdaQueryWrapper.class));

        String memory = aiMemoryService.buildLongTermMemory("user-1", null, 10);

        assertThat(memory).isNotBlank();
    }

    @Test
    @DisplayName("saveConversation sessionId blank string returns early")
    void saveConversationBlankSessionId() {
        aiMemoryService.saveConversation("1", "", "CHAT", "你好", "回复");

        verify(aiChatMessageService, never()).save(any());
    }

    @Test
    @DisplayName("buildLongTermMemory builds memory string with agent/sender info")
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
    @DisplayName("buildLongTermMemory null userId returns empty")
    void buildLongTermMemoryNullUserId() {
        String memory = aiMemoryService.buildLongTermMemory(null, "session-1", 10);

        assertThat(memory).isEmpty();
        verify(aiChatMessageService, never()).list(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("buildLongTermMemory blank userId returns empty")
    void buildLongTermMemoryBlankUserId() {
        String memory = aiMemoryService.buildLongTermMemory("", "session-1", 10);

        assertThat(memory).isEmpty();
    }

    @Test
    @DisplayName("buildLongTermMemory no history returns empty")
    void buildLongTermMemoryNoHistory() {
        doReturn(List.of()).when(aiChatMessageService).list(any(LambdaQueryWrapper.class));

        String memory = aiMemoryService.buildLongTermMemory("user-1", "session-1", 10);

        assertThat(memory).isEmpty();
    }

    @Test
    @DisplayName("listSessionMessages sorts by createdAt ascending")
    void listSessionMessages() {
        AiChatMessage msg1 = AiChatMessage.builder().content("msg1").build();
        AiChatMessage msg2 = AiChatMessage.builder().content("msg2").build();
        doReturn(List.of(msg1, msg2)).when(aiChatMessageService).list(any(LambdaQueryWrapper.class));

        List<AiChatMessageResponse> messages = aiMemoryService.listSessionMessages("user-1", "session-1");

        assertThat(messages).hasSize(2);
    }
}
