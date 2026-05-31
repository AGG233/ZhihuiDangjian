package com.rauio.smartdangjian.server.ai.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.rauio.smartdangjian.server.ai.constants.AiChatResponseType;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatMessageResponse;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.user.service.UserService;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserChatControllerTest {

    @Mock
    private LLMService llmService;

    @Mock
    private AiMemoryService aiMemoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserChatController controller;

    @Test
    @DisplayName("chat 返回 LLMService 的 SSE 响应流并传递原始请求")
    void chatReturnsServiceFluxAndPassesRequest() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest("session-1", "你好");
        AiChatResponse response = new AiChatResponse(AiChatResponseType.TEXT, "session-1", "回复", "chat", "coordinator");
        when(llmService.chat(any(AiChatRequest.class))).thenReturn(Flux.just(response));

        Flux<AiChatResponse> result = controller.chat(request);

        StepVerifier.create(result).expectNext(response).verifyComplete();
        ArgumentCaptor<AiChatRequest> requestCaptor = ArgumentCaptor.forClass(AiChatRequest.class);
        verify(llmService).chat(requestCaptor.capture());
        assertThat(requestCaptor.getValue().sessionId()).isEqualTo("session-1");
        assertThat(requestCaptor.getValue().message()).isEqualTo("你好");
    }

    @Test
    @DisplayName("chat 支持服务层返回空流")
    void chatAllowsEmptyServiceFlux() throws GraphRunnerException {
        AiChatRequest request = new AiChatRequest(null, "请介绍党史");
        when(llmService.chat(request)).thenReturn(Flux.empty());

        StepVerifier.create(controller.chat(request)).verifyComplete();
        verify(llmService).chat(request);
    }

    @Test
    @DisplayName("listMessages 使用当前用户 ID 查询指定会话消息")
    void listMessagesUsesCurrentUserAndSessionId() {
        AiChatMessageResponse message = AiChatMessageResponse.builder()
                .id(1L)
                .sessionId("session-1")
                .userId(100L)
                .agentType("CHAT")
                .senderType("USER")
                .content("你好")
                .messageType("TEXT")
                .createdAt(LocalDateTime.of(2026, 5, 31, 10, 0))
                .build();
        when(userService.getCurrentUserId()).thenReturn("100");
        when(aiMemoryService.listSessionMessages("100", "session-1")).thenReturn(List.of(message));

        var result = controller.listMessages("session-1");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo(1L);
            assertThat(item.getSessionId()).isEqualTo("session-1");
            assertThat(item.getContent()).isEqualTo("你好");
        });
        verify(aiMemoryService).listSessionMessages("100", "session-1");
    }

    @Test
    @DisplayName("listMessages 会话无消息时返回空列表")
    void listMessagesReturnsEmptyList() {
        when(userService.getCurrentUserId()).thenReturn("100");
        when(aiMemoryService.listSessionMessages("100", "empty-session")).thenReturn(List.of());

        var result = controller.listMessages("empty-session");

        assertThat(result.getData()).isEmpty();
    }

    @Test
    @DisplayName("chat 方法声明 SSE media type")
    void chatDeclaresSseMediaType() throws NoSuchMethodException {
        Method method = UserChatController.class.getDeclaredMethod("chat", AiChatRequest.class);

        PostMapping postMapping = method.getAnnotation(PostMapping.class);

        assertThat(postMapping).isNotNull();
        assertThat(postMapping.produces()).contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    @Test
    @DisplayName("listMessages 方法映射会话消息路径")
    void listMessagesDeclaresSessionMessagesPath() throws NoSuchMethodException {
        Method method = UserChatController.class.getDeclaredMethod("listMessages", String.class);

        GetMapping getMapping = method.getAnnotation(GetMapping.class);

        assertThat(getMapping).isNotNull();
        assertThat(getMapping.value()).contains("/{sessionId}/messages");
    }
}
