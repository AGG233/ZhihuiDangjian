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
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

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
    public Flux<AiChatResponse> chat(AiChatRequest request) throws GraphRunnerException {
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
    public Flux<AiChatResponse> quiz(AiQuizRequest request) throws GraphRunnerException {
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
    public Flux<AiChatResponse> evaluate(AiEvaluationRequest request) throws GraphRunnerException {
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

    private Flux<AiChatResponse> stream(AiAgentRunContext context) throws GraphRunnerException {
        String sessionId = normalizeSessionId(context.sessionId());
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(sessionId)
                .addMetadata("sessionId", sessionId)
                .addMetadata("userId", context.userId())
                .build();
        AtomicReference<String> finalOutput = new AtomicReference<>("");

        return aiAgentRegistry.get(context.agentType()).stream(context.input(), runnableConfig)
                .handle((output, sink) -> sink.next(buildAiChatResponse(sessionId, context.agentType().agentName(), output, finalOutput)))
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(() -> aiMemoryService.saveConversation(
                        context.userId(),
                        sessionId,
                        context.agentType().code(),
                        context.input(),
                        finalOutput.get()
                ));
    }

    private static AiChatResponse buildAiChatResponse(String sessionId,
                                                      String agentName,
                                                      NodeOutput output,
                                                      AtomicReference<String> finalOutput) {

        if (output instanceof StreamingOutput streamingOutput) {
            OutputType type = streamingOutput.getOutputType();
            Message message = streamingOutput.message();


            if (type == OutputType.AGENT_MODEL_STREAMING) {
                return new AiChatResponse("THINKING", sessionId, streamingOutput.message().getText(), output.node(), agentName);
            } else if (type == OutputType.AGENT_MODEL_FINISHED) {
                log.debug("AI模型输出完成, agent={}, sessionId={}", agentName, sessionId);
            }

            if (type == OutputType.AGENT_MODEL_FINISHED && message instanceof AssistantMessage am) {
                if (am.hasToolCalls()) {
                    return new AiChatResponse("TOOL_CALL", sessionId, am.getToolCalls().toString(), output.node(), agentName);
                }
                finalOutput.set(am.getText());
                return new AiChatResponse("FINISHED", sessionId, am.getText(), output.node(), agentName);
            }

            if (type == OutputType.AGENT_TOOL_FINISHED && message instanceof ToolResponseMessage tr) {
                return new AiChatResponse("TOOL_RESULT", sessionId, tr.getResponses().toString(), output.node(), agentName);
            }
        }
        return new AiChatResponse("OTHER", sessionId, "Processing...", output.node(), agentName);
    }

    private String normalizeSessionId(String requestedSessionId) {
        if (requestedSessionId == null || requestedSessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestedSessionId;
    }
}
