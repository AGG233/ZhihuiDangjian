package com.rauio.smartdangjian.server.ai.controller.user;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiEvaluationRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiQuizRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "AI聊天接口", description = "提供AI聊天相关功能")
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class UserChatController {

    private final LLMService llmService;
    private final AiMemoryService aiMemoryService;
    private final UserService userService;

    @Operation(summary = "AI文本聊天接口", description = "SSE流式返回纯文本回复")
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> chat(@RequestBody @Valid AiChatRequest request) throws GraphRunnerException {
        return llmService.chat(request);
    }

    @Operation(summary = "AI学习评估接口", description = "SSE流式返回基于学习记录和答题记录的学习评估")
    @PostMapping(value = "/evaluation", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> evaluate(@RequestBody @Valid AiEvaluationRequest request) throws GraphRunnerException {
        return llmService.evaluate(request);
    }

    @Operation(summary = "AI测试小题接口", description = "SSE流式返回围绕指定主题生成的测试小题")
    @PostMapping(value = "/quiz", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> quiz(@RequestBody @Valid AiQuizRequest request) throws GraphRunnerException {
        return llmService.quiz(request);
    }

    @Operation(summary = "查询会话消息", description = "返回当前用户指定会话下的历史消息，用于长期记忆与消息面板展示")
    @GetMapping("/{sessionId}/messages")
    public Result<List<AiChatMessage>> listMessages(@PathVariable String sessionId) {
        return Result.ok(aiMemoryService.listSessionMessages(userService.getCurrentUserId(), sessionId));
    }
}
