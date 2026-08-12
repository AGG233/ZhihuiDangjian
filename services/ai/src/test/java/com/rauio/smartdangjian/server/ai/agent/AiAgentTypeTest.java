package com.rauio.smartdangjian.server.ai.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiAgentTypeTest {

    @Test
    @DisplayName("枚举应包含 5 种 Agent 类型")
    void enumCount() {
        assertThat(AiAgentType.values()).hasSize(5);
    }

    @Test
    @DisplayName("STUDY_ASSISTANT 属性正确")
    void studyAssistantProperties() {
        assertThat(AiAgentType.STUDY_ASSISTANT.code()).isEqualTo("STUDY_ASSISTANT");
        assertThat(AiAgentType.STUDY_ASSISTANT.agentName()).isEqualTo("study-assistant-agent");
        assertThat(AiAgentType.STUDY_ASSISTANT.description()).contains("学习");
    }

    @Test
    @DisplayName("CONTENT_DISCOVERY 属性正确")
    void contentDiscoveryProperties() {
        assertThat(AiAgentType.CONTENT_DISCOVERY.code()).isEqualTo("CONTENT_DISCOVERY");
        assertThat(AiAgentType.CONTENT_DISCOVERY.agentName()).isEqualTo("content-discovery-agent");
        assertThat(AiAgentType.CONTENT_DISCOVERY.description()).contains("搜索");
    }

    @Test
    @DisplayName("ASSESSMENT 属性正确")
    void assessmentProperties() {
        assertThat(AiAgentType.ASSESSMENT.code()).isEqualTo("ASSESSMENT");
        assertThat(AiAgentType.ASSESSMENT.agentName()).isEqualTo("assessment-agent");
        assertThat(AiAgentType.ASSESSMENT.description()).contains("评估");
    }

    @Test
    @DisplayName("REVIEW 属性正确")
    void reviewProperties() {
        assertThat(AiAgentType.REVIEW.code()).isEqualTo("REVIEW");
        assertThat(AiAgentType.REVIEW.agentName()).isEqualTo("review-agent");
        assertThat(AiAgentType.REVIEW.description()).contains("审查");
    }

    @Test
    @DisplayName("PROFILE 属性正确")
    void profileProperties() {
        assertThat(AiAgentType.PROFILE.code()).isEqualTo("PROFILE");
        assertThat(AiAgentType.PROFILE.agentName()).isEqualTo("profile-agent");
        assertThat(AiAgentType.PROFILE.description()).contains("画像");
    }

    @Test
    @DisplayName("fromLegacyCode CHAT 映射到 STUDY_ASSISTANT")
    void fromLegacyCodeChat() {
        assertThat(AiAgentType.fromLegacyCode("CHAT")).isEqualTo(AiAgentType.STUDY_ASSISTANT);
    }

    @Test
    @DisplayName("fromLegacyCode QUIZ 映射到 ASSESSMENT")
    void fromLegacyCodeQuiz() {
        assertThat(AiAgentType.fromLegacyCode("QUIZ")).isEqualTo(AiAgentType.ASSESSMENT);
    }

    @Test
    @DisplayName("fromLegacyCode EVALUATION 映射到 ASSESSMENT")
    void fromLegacyCodeEvaluation() {
        assertThat(AiAgentType.fromLegacyCode("EVALUATION")).isEqualTo(AiAgentType.ASSESSMENT);
    }

    @Test
    @DisplayName("fromLegacyCode 直接使用枚举名转换")
    void fromLegacyCodeDirectValueOf() {
        assertThat(AiAgentType.fromLegacyCode("REVIEW")).isEqualTo(AiAgentType.REVIEW);
        assertThat(AiAgentType.fromLegacyCode("PROFILE")).isEqualTo(AiAgentType.PROFILE);
    }
}
