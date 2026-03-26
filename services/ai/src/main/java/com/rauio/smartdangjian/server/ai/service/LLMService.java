package com.rauio.smartdangjian.server.ai.service;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class LLMService {

    private final ReactAgent chatAgent;

    /**
     * 常规对话
     */
    public Flux<AiChatResponse> chat(AiChatRequest request) throws GraphRunnerException {
        return chatAgent.stream(request.message()).handle((output, sink) -> {
            try {
                sink.next(buildAiChatResponse(request, output));
            } catch (GraphRunnerException e) {
                sink.error(new RuntimeException(e));
            }
        });
    }

    private static AiChatResponse buildAiChatResponse(AiChatRequest request,NodeOutput output) throws GraphRunnerException {

        if (output instanceof StreamingOutput streamingOutput) {
            OutputType type = streamingOutput.getOutputType();
            Message message = streamingOutput.message();


            if (type == OutputType.AGENT_MODEL_STREAMING) {
                System.out.print(streamingOutput.message().getText());
                return new AiChatResponse("THINKING", streamingOutput.message().getText(), output.node());
            } else if (type == OutputType.AGENT_MODEL_FINISHED) {
                System.out.println("\n模型输出完成");
            }

            if (type == OutputType.AGENT_MODEL_FINISHED && message instanceof AssistantMessage am) {
                if (am.hasToolCalls()) {
                    return new AiChatResponse("TOOL_CALL", am.getToolCalls().toString(), output.node());
                }
                return new AiChatResponse("FINISHED", "Model process complete", output.node());
            }

            if (type == OutputType.AGENT_TOOL_FINISHED && message instanceof ToolResponseMessage tr) {
                return new AiChatResponse("TOOL_RESULT", tr.getResponses().toString(), output.node());
            }
        }
        return new AiChatResponse("OTHER", "Processing...", output.node());
    }

//    /**
//     * 评估/反馈
//     */
//    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> evaluate() {
//        String systemPrompt = joinPrompts(promptService.getPrompts(PromptService.PROMPT_TYPE_EVALUATION));
//        return internalCall(systemPrompt, "", null);
//    }
//
//    /**
//     * 测验生成
//     */
//    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> quiz(String topic, String sessionId) {
//        String systemPrompt = joinPrompts(promptService.getPrompts(PromptService.PROMPT_TYPE_QUIZ));
//        String userPrompt = "请围绕该主题出题：" + topic;
//        return internalCall(systemPrompt, userPrompt, sessionId);
//    }
//
//    /**
//     * SSE、流式、落库
//     *
//     * @param systemPrompt 系统提示词
//     * @param userPrompt   用户提示词
//     * @param sessionId    会话ID (可选)
//     */
//    private Flux<ServerSentEvent<Result<AiChatStreamResponse>>> internalCall(
//            String systemPrompt,
//            String userPrompt,
//            String sessionId
//    ) {
//        UserVO user = userConvertor.toVO(userService.getCurrentUser());
//
//        return chatClient.prompt()
//                .system(systemPrompt)
//                .user(userPrompt)
//                .tools(
//                        recentLearningRecordsTool,
//                        learnedCourseTool,
//                        saveQuizReasoningTool,
//                        getQuizReasoningTool
//                )
//                .toolContext(Map.of("CURRENT_USER", user))
//                .stream()
//                .content()
//                .filter(Objects::nonNull)
//                .index()
//                .map(tuple -> {
//                    AiChatStreamResponse resp = new AiChatStreamResponse(tuple.getT2());
//                    if (sessionId != null && !sessionId.isEmpty()){
//                        resp.setSessionId(sessionId);
//                    }
//                    return ServerSentEvent.builder(Result.ok(resp))
//                            .id(String.valueOf(tuple.getT1()))
//                            .event("message")
//                            .build();
//                });
//    }
//
    /**
     * 拼接多条系统提示词。
     *
     * @param systemPrompts 系统提示词列表
     * @return 合并后的提示词文本
     */
    private String joinPrompts(List<String> systemPrompts) {
        if (systemPrompts == null || systemPrompts.isEmpty()) {
            return "";
        }
        return String.join("\n", systemPrompts);
    }
}
