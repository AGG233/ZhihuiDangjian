package com.rauio.smartdangjian.server.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import com.alibaba.cloud.ai.graph.NodeOutput;
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

    @Test
    @DisplayName("GraphRunnerException 时返回 error Flux")
    void graphRunnerException() {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenAnswer(inv -> { throw new GraphRunnerException("图运行错误"); });

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .expectError(GraphRunnerException.class)
                .verify();
    }

    @Test
    @DisplayName("AI 响应超时返回 ERROR 事件")
    void timeoutReturnsError() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.never());

        StepVerifier.withVirtualTime(() -> llmService.chat(request))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.START);
                    assertThat(r.sessionId()).isEqualTo("session-1");
                })
                .thenAwait(Duration.ofSeconds(121))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.ERROR);
                    assertThat(r.output()).contains("响应超时");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("Agent 输出完成时工具调用事件返回 TOOL_CALL")
    void toolCallAssistantMessage() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        StreamingOutput output = mock(StreamingOutput.class);
        lenient().when(output.getOutputType()).thenReturn(OutputType.AGENT_MODEL_FINISHED);
        lenient().when(output.node()).thenReturn("chat-agent");

        AssistantMessage am = mock(AssistantMessage.class);
        when(am.hasToolCalls()).thenReturn(true);
        when(am.getToolCalls()).thenReturn(List.of());
        lenient().when(am.getText()).thenReturn("");
        when(output.message()).thenReturn(am);

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(output));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.TOOL_CALL))
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("Agent 工具执行完成事件返回 TOOL_RESULT")
    void toolResponseMessage() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        StreamingOutput output = mock(StreamingOutput.class);
        lenient().when(output.getOutputType()).thenReturn(OutputType.AGENT_TOOL_FINISHED);
        lenient().when(output.node()).thenReturn("tool-agent");

        ToolResponseMessage trm = mock(ToolResponseMessage.class);
        ToolResponseMessage.ToolResponse tr = new ToolResponseMessage.ToolResponse(null, "test-tool", "执行成功");
        when(trm.getResponses()).thenReturn(List.of(tr));
        when(output.message()).thenReturn(trm);

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(output));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.TOOL_RESULT);
                    assertThat(r.output()).contains("执行成功");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("不匹配类型的 Agent 输出返回 OTHER 事件")
    void nonMatchingOutputTypeReturnsOther() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        StreamingOutput output = mock(StreamingOutput.class);
        lenient().when(output.getOutputType()).thenReturn(OutputType.AGENT_MODEL_FINISHED);
        lenient().when(output.node()).thenReturn("some-node");
        when(output.message()).thenReturn(mock(Message.class));

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(output));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.OTHER);
                    assertThat(r.output()).contains("Processing");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("blank sessionId 时自动生成新会话 ID")
    void chatWithBlankSessionId() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("  ", "你好");
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
    @DisplayName("chat null userId 时使用空字符串")
    void chatWithNullUserId() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn(null);
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class))).thenReturn(Flux.empty());

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("Agent node 为 null 时使用 coordinator 作为默认 agentName")
    void agentNodeNullDefaultsToCoordinator() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        StreamingOutput output = mock(StreamingOutput.class);
        lenient().when(output.getOutputType()).thenReturn(OutputType.AGENT_MODEL_STREAMING);
        lenient().when(output.message()).thenReturn(new AssistantMessage("流式文本"));
        when(output.node()).thenReturn(null);

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(output));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.TEXT);
                    assertThat(r.output()).isEqualTo("流式文本");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("finalOutput 为空白时保存默认文本")
    void blankFinalOutputSavesDefaultText() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "消息");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class))).thenReturn(Flux.empty());

        llmService.chat(request).blockLast();

        verify(aiMemoryService).saveConversation(eq("user-1"), eq("session-1"), eq("COORDINATOR"), eq("消息"), eq("[AI 未返回文本内容]"));
    }

    @Test
    @DisplayName("非 StreamingOutput 类型的 NodeOutput 返回 OTHER 事件")
    void nonStreamingNodeOutputReturnsOther() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        NodeOutput nonStreamingOutput = mock(NodeOutput.class);
        when(nonStreamingOutput.node()).thenReturn("tool-agent");

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(nonStreamingOutput));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.OTHER);
                    assertThat(r.output()).contains("Processing");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("AGENT_TOOL_FINISHED 但消息不是 ToolResponseMessage 时返回 OTHER")
    void toolFinishedWithNonToolResponseMessage() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        StreamingOutput output = mock(StreamingOutput.class);
        lenient().when(output.getOutputType()).thenReturn(OutputType.AGENT_TOOL_FINISHED);
        lenient().when(output.node()).thenReturn("tool-agent");
        when(output.message()).thenReturn(mock(Message.class));

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(output));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.OTHER);
                    assertThat(r.output()).contains("Processing");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("coordinator.stream 抛出 GraphRunnerException 时返回 error Flux")
    void graphRunnerExceptionStream() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);
        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenAnswer(inv -> { throw new GraphRunnerException("图运行错误"); });

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .expectError(GraphRunnerException.class)
                .verify();
    }

    @Test
    @DisplayName("Agent 模型完成且消息带工具调用时返回 TOOL_CALL 事件")
    void agentModelFinishedWithToolCalls() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        StreamingOutput output = mock(StreamingOutput.class);
        lenient().when(output.getOutputType()).thenReturn(OutputType.AGENT_MODEL_FINISHED);
        lenient().when(output.node()).thenReturn("chat-agent");

        AssistantMessage am = mock(AssistantMessage.class);
        when(am.hasToolCalls()).thenReturn(true);
        when(am.getToolCalls()).thenReturn(List.of());
        when(output.message()).thenReturn(am);

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(output));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.TOOL_CALL))
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }

    @Test
    @DisplayName("finalOutput null from AssistantMessage saves default text")
    void finalOutputNullSavesDefaultText() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "消息");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        StreamingOutput finishedOutput = mock(StreamingOutput.class);
        lenient().when(finishedOutput.getOutputType()).thenReturn(OutputType.AGENT_MODEL_FINISHED);
        lenient().when(finishedOutput.node()).thenReturn("chat-agent");

        AssistantMessage am = mock(AssistantMessage.class);
        when(am.hasToolCalls()).thenReturn(false);
        when(am.getText()).thenReturn(null);
        lenient().when(finishedOutput.message()).thenReturn(am);

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(finishedOutput));

        llmService.chat(request).blockLast();

        verify(aiMemoryService).saveConversation(eq("user-1"), eq("session-1"), eq("COORDINATOR"), eq("消息"), eq("[AI 未返回文本内容]"));
    }

    @Test
    @DisplayName("Agent tool finished with ToolResponseMessage returns TOOL_RESULT")
    void agentToolFinishedToolResponseMessage() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        when(userService.getCurrentUserId()).thenReturn("user-1");
        when(aiAgentRegistry.getCoordinator()).thenReturn(coordinator);

        StreamingOutput output = mock(StreamingOutput.class);
        lenient().when(output.getOutputType()).thenReturn(OutputType.AGENT_TOOL_FINISHED);
        lenient().when(output.node()).thenReturn("tool-agent");

        ToolResponseMessage trm = mock(ToolResponseMessage.class);
        ToolResponseMessage.ToolResponse tr = new ToolResponseMessage.ToolResponse("test-tool-call", "test-tool", "成功");
        when(trm.getResponses()).thenReturn(List.of(tr));
        when(output.message()).thenReturn(trm);

        when(coordinator.stream(anyString(), any(RunnableConfig.class)))
                .thenReturn(Flux.just(output));

        Flux<AiChatResponse> result = llmService.chat(request);

        StepVerifier.create(result)
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.START))
                .assertNext(r -> {
                    assertThat(r.type()).isEqualTo(AiChatResponseType.TOOL_RESULT);
                    assertThat(r.output()).contains("成功");
                })
                .assertNext(r -> assertThat(r.type()).isEqualTo(AiChatResponseType.END))
                .verifyComplete();
    }
}
