package com.rauio.smartdangjian.server.ai.controller.user;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiEvaluationRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiQuizRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "AI聊天接口", description = "提供AI聊天相关功能")
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
@PermissionAccess(UserType.STUDENT)
public class UserChatController {

    private final LLMService llmService;
    private final AiMemoryService aiMemoryService;
    private final UserService userService;

    @Operation(summary = "AI智能对话接口", description = "统一入口，SSE流式返回。Coordinator自动识别意图路由到专业Agent。支持通用问答、内容搜索、出题、评估、审查等场景。")
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> chat(@RequestBody @Valid AiChatRequest request) throws GraphRunnerException {
        return llmService.chat(request);
    }

    @Deprecated(since = "0.7.0", forRemoval = true)
    @Operation(summary = "【已弃用】AI学习评估接口", description = "已弃用，请使用统一 POST /api/ai/chat 接口。当前保留向后兼容。")
    @PostMapping(value = "/evaluation", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> evaluate(@RequestBody @Valid AiEvaluationRequest request) throws GraphRunnerException {
        String message = request.message() == null || request.message().isBlank()
                ? "请结合我的学习记录和答题情况生成一份学习评估。"
                : "请结合我的学习记录和答题情况生成学习评估，并重点处理以下要求：" + request.message();
        return llmService.chat(new AiChatRequest(request.sessionId(), message));
    }

    @Deprecated(since = "0.7.0", forRemoval = true)
    @Operation(summary = "【已弃用】AI测试小题接口", description = "已弃用，请使用统一 POST /api/ai/chat 接口。当前保留向后兼容。")
    @PostMapping(value = "/quiz", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> quiz(@RequestBody @Valid AiQuizRequest request) throws GraphRunnerException {
        return llmService.chat(new AiChatRequest(request.sessionId(), "请围绕以下主题生成测试小题：" + request.topic()));
    }

    @Operation(summary = "查询会话消息", description = "返回当前用户指定会话下的历史消息，用于长期记忆与消息面板展示")
    @GetMapping("/{sessionId}/messages")
    public Result<List<AiChatMessage>> listMessages(@PathVariable String sessionId) {
        return Result.ok(aiMemoryService.listSessionMessages(userService.getCurrentUserId(), sessionId));
    }
}
