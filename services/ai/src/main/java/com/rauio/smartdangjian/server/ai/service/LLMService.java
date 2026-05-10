package com.rauio.smartdangjian.server.ai.service;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.rauio.smartdangjian.server.ai.agent.AiAgentRegistry;
import com.rauio.smartdangjian.server.ai.agent.AiAgentType;
import com.rauio.smartdangjian.server.ai.pojo.dto.AiAgentRunContext;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiEvaluationRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiQuizRequest;
import com.rauio.smartdangjian.server.ai.constants.AiChatResponseType;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.UUID;

@Service
@Slf4j
public class LLMService {

    private final AiAgentRegistry aiAgentRegistry;
    private final UserService userService;
    private final AiMemoryService aiMemoryService;

    public LLMService(AiAgentRegistry aiAgentRegistry,
                      UserService userService,
                      AiMemoryService aiMemoryService) {
        this.aiAgentRegistry = aiAgentRegistry;
        this.userService = userService;
        this.aiMemoryService = aiMemoryService;
    }

    /**
     * 常规对话
     */
    public Flux<AiChatResponse> chat(AiChatRequest request) {
        return stream(new AiAgentRunContext(
                AiAgentType.CHAT,
                request.sessionId(),
                userService.getCurrentUserId(),
                request.message()
        ));
    }

    /**
     * 生成测试小题
     */
    public Flux<AiChatResponse> quiz(AiQuizRequest request) {
        return stream(new AiAgentRunContext(
                AiAgentType.QUIZ,
                request.sessionId(),
                userService.getCurrentUserId(),
                "请围绕以下主题生成测试小题：" + request.topic()
        ));
    }

    /**
     * 生成学习评估
     */
    public Flux<AiChatResponse> evaluate(AiEvaluationRequest request) {
        String prompt = request.message() == null || request.message().isBlank()
                ? "请结合我的学习记录和答题情况生成一份学习评估。"
                : "请结合我的学习记录和答题情况生成学习评估，并重点处理以下要求：" + request.message();
        return stream(new AiAgentRunContext(
                AiAgentType.EVALUATION,
                request.sessionId(),
                userService.getCurrentUserId(),
                prompt
        ));
    }

    private Flux<AiChatResponse> stream(AiAgentRunContext context) {
        return Flux.defer(() -> {
            String sessionId = normalizeSessionId(context.sessionId());
            String agentName = context.agentType().agentName();
            String agentCode = context.agentType().code();
            String userId = context.userId() != null ? context.userId() : "";
            RunnableConfig runnableConfig = RunnableConfig.builder()
                    .threadId(sessionId)
                    .addMetadata("sessionId", sessionId)
                    .addMetadata("userId", userId)
                    .build();
            AtomicReference<String> finalOutput = new AtomicReference<>("");

            log.info("AI请求开始 agent={} sessionId={} userId={}", agentName, sessionId, userId);
            try {
                return Flux.concat(
                                Flux.just(new AiChatResponse(AiChatResponseType.START, sessionId, "", "start", agentName)),
                                aiAgentRegistry.get(context.agentType()).stream(context.input(), runnableConfig)
                                        .<AiChatResponse>handle((output, sink) -> sink.next(buildAiChatResponse(sessionId, agentName, output, finalOutput)))
                                        .publishOn(Schedulers.boundedElastic())
                                        .timeout(Duration.ofSeconds(120),
                                                Flux.just(new AiChatResponse(AiChatResponseType.ERROR, sessionId, "AI 响应超时，请稍后重试", "error", agentName)))
                                        .doOnComplete(() -> {
                                            String output = finalOutput.get();
                                            if (output == null || output.isBlank()) {
                                                output = "[AI 未返回文本内容]";
                                            }
                                            aiMemoryService.saveConversation(userId, sessionId, agentCode, context.input(), output);
                                            log.info("AI请求完成 agent={} sessionId={}", agentName, sessionId);
                                        })
                                        .doOnCancel(() -> log.warn("客户端断开连接 agent={} sessionId={}", agentName, sessionId))
                                        .doOnError(e -> log.error("AI流式错误 agent={} sessionId={}", agentName, sessionId, e))
                                        .onErrorResume(e -> Flux.just(
                                                new AiChatResponse(AiChatResponseType.ERROR, sessionId, "AI 服务暂时不可用，请稍后重试", "error", agentName)))
                        )
                        .concatWith(Flux.just(new AiChatResponse(AiChatResponseType.END, sessionId, "", "end", agentName)));
            } catch (GraphRunnerException e) {
                return Flux.error(e);
            }
        });
    }

    private static AiChatResponse buildAiChatResponse(String sessionId,
                                                      String agentName,
                                                      NodeOutput output,
                                                      AtomicReference<String> finalOutput) {

        if (output instanceof StreamingOutput streamingOutput) {
            OutputType type = streamingOutput.getOutputType();
            Message message = streamingOutput.message();


            if (type == OutputType.AGENT_MODEL_STREAMING) {
                return new AiChatResponse(AiChatResponseType.TEXT, sessionId, streamingOutput.message().getText(), output.node(), agentName);
            } else if (type == OutputType.AGENT_MODEL_FINISHED) {
                log.debug("AI模型输出完成, agent={}, sessionId={}", agentName, sessionId);
            }

            if (type == OutputType.AGENT_MODEL_FINISHED && message instanceof AssistantMessage am) {
                if (am.hasToolCalls()) {
                    return new AiChatResponse(AiChatResponseType.TOOL_CALL, sessionId, am.getToolCalls().toString(), output.node(), agentName);
                }
                finalOutput.set(am.getText());
                return new AiChatResponse(AiChatResponseType.FINISHED, sessionId, am.getText(), output.node(), agentName);
            }

            if (type == OutputType.AGENT_TOOL_FINISHED && message instanceof ToolResponseMessage tr) {
                return new AiChatResponse(AiChatResponseType.TOOL_RESULT, sessionId, tr.getResponses().toString(), output.node(), agentName);
            }
        }
        return new AiChatResponse(AiChatResponseType.OTHER, sessionId, "Processing...", output.node(), agentName);
    }

    private String normalizeSessionId(String requestedSessionId) {
        if (requestedSessionId == null || requestedSessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestedSessionId;
    }
}
