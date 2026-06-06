package com.rauio.smartdangjian.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiPromptConstantsTest {

    @Test
    @DisplayName("COMMON_SYSTEM_PROMPTS 包含 4 条提示")
    void commonSystemPrompts() {
        assertThat(AiPromptConstants.COMMON_SYSTEM_PROMPTS).hasSize(4);
        assertThat(AiPromptConstants.COMMON_SYSTEM_PROMPTS.get(0)).contains("党务学习助手");
    }

    @Test
    @DisplayName("EVALUATION_SYSTEM_PROMPTS 包含 3 条提示")
    void evaluationSystemPrompts() {
        assertThat(AiPromptConstants.EVALUATION_SYSTEM_PROMPTS).hasSize(3);
        assertThat(AiPromptConstants.EVALUATION_SYSTEM_PROMPTS.get(0)).contains("学习进度评价");
    }

    @Test
    @DisplayName("QUIZ_SYSTEM_PROMPTS 包含 4 条提示")
    void quizSystemPrompts() {
        assertThat(AiPromptConstants.QUIZ_SYSTEM_PROMPTS).hasSize(4);
        assertThat(AiPromptConstants.QUIZ_SYSTEM_PROMPTS.get(0)).contains("测验助手");
    }

    @Test
    @DisplayName("常量列表不可变")
    void listsAreUnmodifiable() {
        assertThat(AiPromptConstants.COMMON_SYSTEM_PROMPTS).isUnmodifiable();
        assertThat(AiPromptConstants.EVALUATION_SYSTEM_PROMPTS).isUnmodifiable();
        assertThat(AiPromptConstants.QUIZ_SYSTEM_PROMPTS).isUnmodifiable();
    }
}
