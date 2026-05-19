package com.rauio.smartdangjian.server.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.rauio.smartdangjian.server.ai.agent.AiAgentRegistry;
import com.rauio.smartdangjian.server.ai.constants.AiChatResponseType;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LLMServiceTest {

    @Mock
    private AiAgentRegistry aiAgentRegistry;

    @Mock
    private UserService userService;

    @Mock
    private AiMemoryService aiMemoryService;

    @Mock
    private LlmRoutingAgent coordinator;

    private LLMService llmService;

    @BeforeEach
    void setUp() {
        llmService = new LLMService(aiAgentRegistry, userService, aiMemoryService);
    }

    @Test
    @DisplayName("chat 返回包含 START 和 END 事件的 Flux 流")
    void chatReturnsStartAndEnd() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class))).thenReturn(Flux.empty());

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.START);
                    assertThat(r.sessionId()).isEqualTo("session-1");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("chat 处理流式文本输出并保存对话")
    void chatHandlesStreamingTextAndSaves() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        StreamingOutput streamingOutput = mock(StreamingOutput.class);
        lenient().when(streamingOutput.getOutputType()).thenReturn(OutputType.AGENT_MODEL_STREAMING);
        lenient().when(streamingOutput.message()).thenReturn(new AssistantMessage("正在思考..."));
        lenient().when(streamingOutput.node()).thenReturn("chat-agent");

        StreamingOutput finishedOutput = mock(StreamingOutput.class);
        lenient().when(finishedOutput.getOutputType()).thenReturn(OutputType.AGENT_MODEL_FINISHED);
        lenient().when(finishedOutput.message()).thenReturn(new AssistantMessage("最终回复"));
        lenient().when(finishedOutput.node()).thenReturn("chat-agent");

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(streamingOutput, finishedOutput));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.TEXT);
                    assertThat(r.output()).isEqualTo("正在思考...");
                })
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.FINISHED);
                    assertThat(r.output()).isEqualTo("最终回复");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();

        verify(aiMemoryService).saveConversation("user-1", "session-1", "COORDINATOR", "你好", "最终回复");
    }

    @Test
    @DisplayName("chat 流式错误时触发 onErrorResume 返回 ERROR 事件")
    void chatErrorResume() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.error(new RuntimeException("模拟错误")));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.ERROR);
                    assertThat(r.output()).contains("AI 服务暂时不可用");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("chat 空消息也返回 START 和 END")
    void chatWithBlankInput() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class))).thenReturn(Flux.empty());

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("chat null sessionId 时自动生成新的会话 ID")
    void chatWithNullSessionId() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest(null, "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class))).thenReturn(Flux.empty());

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.START);
                    assertThat(r.sessionId()).isNotNull();
                    assertThat(r.sessionId()).isNotEmpty();
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("chat output 为空时保存默认文本")
    void chatSavesDefaultTextWhenOutputEmpty() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class))).thenReturn(Flux.empty());

        llmService.chat(request).blockLast();

        verify(aiMemoryService).saveConversation("user-1", "session-1", "COORDINATOR", "你好", "[AI 未返回文本内容]");
    }
}
