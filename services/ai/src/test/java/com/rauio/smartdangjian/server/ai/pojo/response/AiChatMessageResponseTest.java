package com.rauio.smartdangjian.server.ai.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;

class AiChatMessageResponseTest {

    @Test
    @DisplayName("fromEntity with null entity returns null")
    void fromEntityNull() {
        assertThat(AiChatMessageResponse.fromEntity(null)).isNull();
    }

    @Test
    @DisplayName("fromEntity maps all fields from entity")
    void fromEntity() {
        LocalDateTime now = LocalDateTime.now();
        AiChatMessage entity = AiChatMessage.builder()
                .id(1L)
                .sessionId("session-1")
                .userId(100L)
                .agentType("CHAT")
                .senderType("user")
                .content("hello")
                .messageType("text")
                .metadata(Map.of("key", "value"))
                .createdAt(now)
                .build();

        AiChatMessageResponse response = AiChatMessageResponse.fromEntity(entity);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSessionId()).isEqualTo("session-1");
        assertThat(response.getUserId()).isEqualTo(100L);
        assertThat(response.getAgentType()).isEqualTo("CHAT");
        assertThat(response.getSenderType()).isEqualTo("user");
        assertThat(response.getContent()).isEqualTo("hello");
        assertThat(response.getMessageType()).isEqualTo("text");
        assertThat(response.getMetadata()).containsEntry("key", "value");
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }
}
