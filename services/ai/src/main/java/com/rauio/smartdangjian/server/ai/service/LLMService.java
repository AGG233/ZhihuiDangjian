package com.rauio.smartdangjian.server.ai.service;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.rauio.smartdangjian.server.ai.agent.AiAgentRegistry;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
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
     * 统一入口：用户消息通过 Coordinator 自动路由到合适的专业 Agent
     */
    public Flux<AiChatResponse> chat(AiChatRequest request) {
        return stream(request.sessionId(), userService.getCurrentUserId(), request.message());
    }

    private Flux<AiChatResponse> stream(String providedSessionId, String userId, String input) {
        return Flux.defer(() -> {
            String sessionId = normalizeSessionId(providedSessionId);
            String uid = userId != null ? userId : "";
            RunnableConfig runnableConfig = RunnableConfig.builder()
                    .threadId(sessionId)
                    .addMetadata("sessionId", sessionId)
                    .addMetadata("userId", uid)
                    .build();
            AtomicReference<String> finalOutput = new AtomicReference<>("");

            log.info("AI请求开始 sessionId={} userId={}", sessionId, uid);
            try {
                return Flux.concat(
                        Flux.just(new AiChatResponse(AiChatResponseType.START, sessionId, "", "start", "coordinator")),
                        aiAgentRegistry.getCoordinator().stream(input, runnableConfig)
                                .<AiChatResponse>handle((output, sink) -> {
                                    String agentName = output.node() != null ? output.node() : "coordinator";
                                    sink.next(buildAiChatResponse(sessionId, agentName, output, finalOutput));
                                })
                                .publishOn(Schedulers.boundedElastic())
                                .timeout(Duration.ofSeconds(120),
                                        Flux.just(new AiChatResponse(AiChatResponseType.ERROR, sessionId, "AI 响应超时，请稍后重试", "error", "coordinator")))
                                .doOnComplete(() -> {
                                    String output = finalOutput.get();
                                    if (output == null || output.isBlank()) {
                                        output = "[AI 未返回文本内容]";
                                    }
                                    aiMemoryService.saveConversation(uid, sessionId, "COORDINATOR", input, output);
                                    log.info("AI请求完成 sessionId={}", sessionId);
                                })
                                .doOnCancel(() -> log.warn("客户端断开连接 sessionId={}", sessionId))
                                .doOnError(e -> log.error("AI流式错误 sessionId={}", sessionId, e))
                                .onErrorResume(e -> Flux.just(
                                        new AiChatResponse(AiChatResponseType.ERROR, sessionId, "AI 服务暂时不可用，请稍后重试", "error", "coordinator")))
                ).concatWith(Flux.just(new AiChatResponse(AiChatResponseType.END, sessionId, "", "end", "coordinator")));
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
