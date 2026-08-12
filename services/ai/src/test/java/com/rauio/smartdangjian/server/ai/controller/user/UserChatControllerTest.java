package com.rauio.smartdangjian.server.ai.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.rauio.smartdangjian.server.ai.pojo.request.AiEvaluationRequest;
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
    @DisplayName("evaluate with null message uses default prompt")
    void evaluateNullMessage() throws GraphRunnerException {
        when(llmService.chat(any())).thenReturn(Flux.empty());

        var request = new AiEvaluationRequest("session-1", null);
        Flux<AiChatResponse> result = controller.evaluate(request);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    @DisplayName("evaluate with blank message uses default prompt")
    void evaluateBlankMessage() throws GraphRunnerException {
        when(llmService.chat(any())).thenReturn(Flux.empty());

        var request = new AiEvaluationRequest("session-1", "  ");
        Flux<AiChatResponse> result = controller.evaluate(request);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    @DisplayName("evaluate with non-blank message appends to request")
    void evaluateWithMessage() throws GraphRunnerException {
        when(llmService.chat(any())).thenReturn(Flux.empty());

        var request = new AiEvaluationRequest("session-1", "重点分析学习情况");
        Flux<AiChatResponse> result = controller.evaluate(request);

        StepVerifier.create(result).verifyComplete();
    }
}
