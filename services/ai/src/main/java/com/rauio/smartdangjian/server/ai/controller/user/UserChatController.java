package com.rauio.smartdangjian.server.ai.controller.user;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Tag(name = "AI聊天接口", description = "提供AI聊天相关功能")
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class UserChatController {

    private final LLMService llmService;

    @Operation(summary = "AI文本聊天接口", description = "SSE流式返回纯文本回复")
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> chat(@RequestBody @Valid AiChatRequest request) throws GraphRunnerException {
        return llmService.chat(request);
    }

//    @Operation(summary = "AI个人评价接口", description = "SSE流式返回个人评价")
//    @GetMapping(value = "/evaluate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> evaluate() {
//        return llmService.evaluate();
//    }
//
//    @Operation(summary = "AI quiz接口", description = "SSE流式返回 quiz")
//    @GetMapping(value = "/quiz", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> quiz(String topic, String sessionId) {
//        return llmService.quiz(topic, sessionId);
//    }
}
