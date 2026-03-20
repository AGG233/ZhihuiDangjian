package com.rauio.smartdangjian.controller.ai;

import com.rauio.smartdangjian.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.pojo.response.AiChatStreamResponse;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.service.ai.LLMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import jakarta.validation.Valid;

@Tag(name = "AI聊天接口", description = "提供AI聊天相关功能")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    private final LLMService llmService;

    @Operation(summary = "AI文本聊天接口", description = "SSE流式返回纯文本回复")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> chat(@RequestBody @Valid AiChatRequest request) {
        return llmService.chat(request);
    }

    @Operation(summary = "AI个人评价接口", description = "SSE流式返回个人评价")
    @GetMapping(value = "/evaluate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> evaluate() {
        return llmService.evaluate();
    }

    @Operation(summary = "AI quiz接口", description = "SSE流式返回 quiz")
    @GetMapping(value = "/quiz", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> quiz(String topic, String sessionId) {
        return llmService.quiz(topic, sessionId);
    }
}
