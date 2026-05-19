package com.rauio.smartdangjian.server.ai.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.SystemMessage;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.PromptService;

@ExtendWith(MockitoExtension.class)
class DynamicSystemPromptInterceptorTest {

    @Mock
    private PromptService promptService;

    @Mock
    private AiMemoryService aiMemoryService;

    @Mock
    private ModelRequest request;

    @Mock
    private ModelCallHandler handler;

    @Mock
    private ModelResponse response;

    @Captor
    private ArgumentCaptor<ModelRequest> requestCaptor;

    private DynamicSystemPromptInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new DynamicSystemPromptInterceptor(AiAgentType.STUDY_ASSISTANT, promptService, aiMemoryService);
    }

    @Test
    @DisplayName("getName 返回基于 agentType code 的拦截器名称")
    void getName() {
        assertThat(interceptor.getName()).isEqualTo("dynamic-system-prompt-study_assistant");
    }

    @Test
    @DisplayName("interceptModel 构建包含系统提示词和长期记忆的 system message")
    void interceptModelBuildsMessageWithPromptAndMemory() {
        Map<String, Object> context = Map.of("userId", "user-1", "sessionId", "session-1");
        when(request.getContext()).thenReturn(context);
        when(request.getSystemMessage()).thenReturn(null);
        when(promptService.buildSystemPrompt("STUDY_ASSISTANT")).thenReturn("你是AI助手");
        when(aiMemoryService.buildLongTermMemory("user-1", "session-1", 12)).thenReturn("用户上次问过党建知识");
        when(handler.call(any())).thenReturn(response);

        interceptor.interceptModel(request, handler);

        verify(handler).call(requestCaptor.capture());
        ModelRequest captured = requestCaptor.getValue();
        SystemMessage systemMessage = captured.getSystemMessage();
        assertThat(systemMessage).isNotNull();
        assertThat(systemMessage.getText()).contains("你是AI助手");
        assertThat(systemMessage.getText()).contains("用户上次问过党建知识");
        assertThat(systemMessage.getText()).contains("Long Term Memory");
    }

    @Test
    @DisplayName("interceptModel 长期记忆为空时不追加 memory 段落")
    void interceptModelWithoutMemory() {
        Map<String, Object> context = Map.of("userId", "user-1", "sessionId", "session-1");
        when(request.getContext()).thenReturn(context);
        when(request.getSystemMessage()).thenReturn(null);
        when(promptService.buildSystemPrompt("STUDY_ASSISTANT")).thenReturn("你是AI助手");
        when(aiMemoryService.buildLongTermMemory("user-1", "session-1", 12)).thenReturn("");
        when(handler.call(any())).thenReturn(response);

        interceptor.interceptModel(request, handler);

        verify(handler).call(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getSystemMessage().getText()).isEqualTo("你是AI助手");
    }

    @Test
    @DisplayName("interceptModel 保留已有的 system message 并追加内容")
    void interceptModelAppendsToExistingSystemMessage() {
        SystemMessage existingMsg = new SystemMessage("已有系统提示");
        Map<String, Object> context = Map.of("userId", "user-1", "sessionId", "session-1");
        when(request.getContext()).thenReturn(context);
        when(request.getSystemMessage()).thenReturn(existingMsg);
        when(promptService.buildSystemPrompt("STUDY_ASSISTANT")).thenReturn("动态提示词");
        when(aiMemoryService.buildLongTermMemory("user-1", "session-1", 12)).thenReturn("记忆内容");
        when(handler.call(any())).thenReturn(response);

        interceptor.interceptModel(request, handler);

        verify(handler).call(requestCaptor.capture());
        String text = requestCaptor.getValue().getSystemMessage().getText();
        assertThat(text).contains("已有系统提示");
        assertThat(text).contains("动态提示词");
        assertThat(text).contains("记忆内容");
    }

    @Test
    @DisplayName("interceptModel context 中无 userId 和 sessionId 时安全处理")
    void interceptModelWithNullContextValues() {
        when(request.getContext()).thenReturn(Map.of());
        when(request.getSystemMessage()).thenReturn(null);
        when(promptService.buildSystemPrompt("STUDY_ASSISTANT")).thenReturn("你是AI助手");
        when(aiMemoryService.buildLongTermMemory(null, null, 12)).thenReturn("");
        when(handler.call(any())).thenReturn(response);

        interceptor.interceptModel(request, handler);

        verify(handler).call(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getSystemMessage().getText()).isEqualTo("你是AI助手");
    }
}
