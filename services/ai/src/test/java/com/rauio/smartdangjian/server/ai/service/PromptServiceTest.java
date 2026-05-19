package com.rauio.smartdangjian.server.ai.service;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.mapper.AiPromptsMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiPrompts;
import com.rauio.smartdangjian.server.ai.pojo.enums.PromptRoleEnum;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    private AiPromptsMapper mapper;

    @Spy
    @InjectMocks
    private PromptService promptService;

    @Captor
    private ArgumentCaptor<AiPrompts> promptCaptor;

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
    @DisplayName("多个提示词以双换行连接")
    void multiplePromptsJoined() {
        AiPrompts p1 = AiPrompts.builder().content("提示词1").build();
        AiPrompts p2 = AiPrompts.builder().content("提示词2").build();
        doReturn(List.of(p1, p2)).when(promptService).listEnabledSystemPrompts("CHAT");

        String prompt = promptService.buildSystemPrompt("CHAT");

        assertThat(prompt).isEqualTo("提示词1\n\n提示词2");
    }

    @Test
    @DisplayName("提示词列表包含空内容时自动过滤")
    void filtersBlankContent() {
        AiPrompts p1 = AiPrompts.builder().content("有效内容").build();
        AiPrompts p2 = AiPrompts.builder().content("").build();
        AiPrompts p3 = AiPrompts.builder().content(null).build();
        doReturn(List.of(p1, p2, p3)).when(promptService).listEnabledSystemPrompts("CHAT");

        String prompt = promptService.buildSystemPrompt("CHAT");

        assertThat(prompt).isEqualTo("有效内容");
    }

    @Test
    @DisplayName("无效角色抛出 IllegalArgumentException")
    void invalidRoleThrowsException() {
        assertThatThrownBy(() -> {
            var method = PromptService.class.getDeclaredMethod("parsePromptRole", String.class);
            method.setAccessible(true);
            method.invoke(promptService, "INVALID_ROLE");
        }).hasCauseInstanceOf(IllegalArgumentException.class)
          .getCause().hasMessageContaining("无效的提示词角色");
    }

    @Test
    @DisplayName("create 构建并保存提示词")
    void create() {
        AiPromptCreateRequest request = new AiPromptCreateRequest();
        request.setAgentType("CHAT");
        request.setName("测试提示词");
        request.setContent("提示词内容");
        request.setRole("SYSTEM");
        request.setEnabled(true);
        request.setSort(5);

        doReturn(true).when(promptService).save(any(AiPrompts.class));

        AiPrompts result = promptService.create(request);

        assertThat(result.getAgentType()).isEqualTo("CHAT");
        assertThat(result.getName()).isEqualTo("测试提示词");
        assertThat(result.getContent()).isEqualTo("提示词内容");
        assertThat(result.getRole()).isEqualTo(PromptRoleEnum.SYSTEM);
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getSort()).isEqualTo(5);
        assertThat(result.getId()).isNull();

        verify(promptService).save(promptCaptor.capture());
        assertThat(promptCaptor.getValue().getAgentType()).isEqualTo("CHAT");
    }

    @Test
    @DisplayName("create 使用默认值: enabled=false, sort=0")
    void createWithDefaults() {
        AiPromptCreateRequest request = new AiPromptCreateRequest();
        request.setAgentType("CHAT");
        request.setName("test");
        request.setContent("content");
        request.setRole("SYSTEM");

        doReturn(true).when(promptService).save(any(AiPrompts.class));

        AiPrompts result = promptService.create(request);

        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getSort()).isZero();
    }

    @Test
    @DisplayName("update 更新已有提示词")
    void update() {
        AiPrompts existing = AiPrompts.builder()
                .id("prompt-1")
                .agentType("CHAT")
                .name("旧名称")
                .content("旧内容")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(false)
                .sort(0)
                .build();

        doReturn(existing).when(promptService).getById("prompt-1");
        doReturn(true).when(promptService).updateById(any(AiPrompts.class));

        AiPromptUpdateRequest request = new AiPromptUpdateRequest();
        request.setName("新名称");
        request.setContent("新内容");
        request.setRole("DEVELOPER");
        request.setEnabled(true);
        request.setSort(10);

        AiPrompts result = promptService.update("prompt-1", request);

        assertThat(result.getName()).isEqualTo("新名称");
        assertThat(result.getContent()).isEqualTo("新内容");
        assertThat(result.getRole()).isEqualTo(PromptRoleEnum.DEVELOPER);
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getSort()).isEqualTo(10);
    }

    @Test
    @DisplayName("update 只更新非 null 字段")
    void updateOnlyNonNullFields() {
        AiPrompts existing = AiPrompts.builder()
                .id("prompt-1")
                .agentType("CHAT")
                .name("名称")
                .content("内容")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(false)
                .sort(0)
                .build();

        doReturn(existing).when(promptService).getById("prompt-1");
        doReturn(true).when(promptService).updateById(any(AiPrompts.class));

        AiPromptUpdateRequest request = new AiPromptUpdateRequest();
        request.setName("仅更新名称");

        AiPrompts result = promptService.update("prompt-1", request);

        assertThat(result.getName()).isEqualTo("仅更新名称");
        assertThat(result.getContent()).isEqualTo("内容");
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("update 提示词不存在时抛出 BusinessException")
    void updateThrowsWhenNotFound() {
        doReturn(null).when(promptService).getById("nonexistent");

        AiPromptUpdateRequest request = new AiPromptUpdateRequest();

        assertThatThrownBy(() -> promptService.update("nonexistent", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("提示词不存在");
    }
}
