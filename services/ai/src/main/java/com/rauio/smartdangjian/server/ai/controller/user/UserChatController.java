package com.rauio.smartdangjian.server.ai.controller.user;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatMessageResponse;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.user.service.UserService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Tag(name = "AI聊天接口", description = "提供AI聊天相关功能")
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.STUDENT)
public class UserChatController {

    private final LLMService llmService;
    private final AiMemoryService aiMemoryService;
    private final UserService userService;

    @Operation(summary = "AI智能对话接口", description = "统一入口，SSE流式返回。Coordinator自动识别意图路由到专业Agent。支持通用问答、内容搜索、出题、评估、审查等场景。")
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> chat(@RequestBody @Valid AiChatRequest request) throws GraphRunnerException {
        return llmService.chat(request);
    }

    @Operation(summary = "查询会话消息", description = "返回当前用户指定会话下的历史消息，用于长期记忆与消息面板展示")
    @GetMapping("/{sessionId}/messages")
    public Result<List<AiChatMessageResponse>> listMessages(@PathVariable String sessionId) {
        return Result.ok(aiMemoryService.listSessionMessages(userService.getCurrentUserId(), sessionId));
    }
}
