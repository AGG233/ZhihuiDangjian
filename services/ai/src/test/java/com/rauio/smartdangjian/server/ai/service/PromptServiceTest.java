package com.rauio.smartdangjian.server.ai.service;

import com.rauio.smartdangjian.server.ai.mapper.AiPromptsMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiPrompts;
import com.rauio.smartdangjian.server.ai.pojo.enums.PromptRoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    private AiPromptsMapper mapper;

    @Spy
    @InjectMocks
    private PromptService promptService;

    @Test
    @DisplayName("空提示词列表返回默认系统提示词")
    void emptyPromptsReturnsDefaultSystemPrompt() {
        doReturn(Collections.emptyList()).when(promptService).listEnabledSystemPrompts("CHAT");

        String prompt = promptService.buildSystemPrompt("CHAT");

        assertThat(prompt).contains("智慧党建平台AI助手");
        assertThat(prompt).contains("使用中文");
    }

    @Test
    @DisplayName("数据库有提示词时优先返回数据库内容")
    void returnsDatabasePrompts() {
        AiPrompts dbPrompt = AiPrompts.builder()
                .content("数据库中的自定义提示词")
                .enabled(true)
                .role(PromptRoleEnum.SYSTEM)
                .agentType("CHAT")
                .sort(0)
                .build();
        doReturn(List.of(dbPrompt)).when(promptService).listEnabledSystemPrompts("CHAT");

        String prompt = promptService.buildSystemPrompt("CHAT");

        assertThat(prompt).isEqualTo("数据库中的自定义提示词");
    }

    @Test
    @DisplayName("无效角色抛出 IllegalArgumentException")
    void invalidRoleThrowsException() {
        assertThatThrownBy(() -> {
            // 通过反射调用私有方法 parsePromptRole 来验证
            var method = PromptService.class.getDeclaredMethod("parsePromptRole", String.class);
            method.setAccessible(true);
            method.invoke(promptService, "INVALID_ROLE");
        }).hasCauseInstanceOf(IllegalArgumentException.class)
          .getCause().hasMessageContaining("无效的提示词角色");
    }
}
