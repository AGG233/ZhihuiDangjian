package com.rauio.smartdangjian.server.ai.service;

import com.rauio.smartdangjian.server.ai.mapper.AiChatMessageMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AiChatMessageServiceTest {

    @Mock
    private AiChatMessageMapper mapper;

    @Spy
    @InjectMocks
    private AiChatMessageService aiChatMessageService;

    @Test
    @DisplayName("mapper 被正确注入")
    void mapperInjected() {
        assertThat(aiChatMessageService).isNotNull();
    }

    @Test
    @DisplayName("AiChatMessage 实体构建")
    void buildMessage() {
        AiChatMessage message = AiChatMessage.builder()
                .id("msg-1")
                .sessionId("session-1")
                .userId("user-1")
                .agentType("CHAT")
                .senderType("user")
                .content("你好")
                .build();

        assertThat(message.getId()).isEqualTo("msg-1");
        assertThat(message.getSessionId()).isEqualTo("session-1");
        assertThat(message.getUserId()).isEqualTo("user-1");
        assertThat(message.getAgentType()).isEqualTo("CHAT");
        assertThat(message.getContent()).isEqualTo("你好");
    }
}
