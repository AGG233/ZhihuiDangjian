package com.rauio.smartdangjian.service.ai;

import com.rauio.smartdangjian.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.pojo.vo.UserVO;
import com.rauio.smartdangjian.service.ai.tool.InfoTool;
import com.rauio.smartdangjian.service.ai.tool.QuizTool;
import com.rauio.smartdangjian.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.rauio.smartdangjian.pojo.response.AiChatStreamResponse;
import com.rauio.smartdangjian.pojo.response.Result;


@Service
@Slf4j
@RequiredArgsConstructor
public class LLMService {

    private final ChatClient chatClient;
    private final InfoTool infoTool;
    private final QuizTool quizTool;
    private final UserConvertor userConvertor;
    private final UserService userService;
    private final PromptService promptService;

    /**
     * 常规对话
     */
    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> chat(AiChatRequest request) {
        String systemPrompt = joinPrompts(promptService.getPrompts(PromptService.PROMPT_TYPE_COMMON));
        return internalCall(systemPrompt, request.getMessage(), request.getSessionId());
    }

    /**
     * 评估/反馈
     */
    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> evaluate() {
        String systemPrompt = joinPrompts(promptService.getPrompts(PromptService.PROMPT_TYPE_EVALUATION));
        return internalCall(systemPrompt, "", null);
    }

    /**
     * 测验生成
     */
    public Flux<ServerSentEvent<Result<AiChatStreamResponse>>> quiz(String topic, String sessionId) {
        String systemPrompt = joinPrompts(promptService.getPrompts(PromptService.PROMPT_TYPE_QUIZ));
        String userPrompt = "请围绕该主题出题：" + topic;
        return internalCall(systemPrompt, userPrompt, sessionId);
    }

    /**
     * 核心业务方法：处理 SSE、流式、落库
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @param sessionId    会话ID (可选)
     */
    private Flux<ServerSentEvent<Result<AiChatStreamResponse>>> internalCall(
            String systemPrompt,
            String userPrompt,
            String sessionId) {

        UserVO user = userConvertor.toVO(userService.getUserFromAuthentication());

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .tools(infoTool, quizTool)
                .toolContext(Map.of("CURRENT_USER", user))
                .stream()
                .content()
                .filter(Objects::nonNull)
                .index()
                .map(tuple -> {
                    AiChatStreamResponse resp = new AiChatStreamResponse(tuple.getT2());
                    if (!sessionId.isEmpty()){
                        resp.setSessionId(sessionId);
                    }
                    return ServerSentEvent.builder(Result.ok(resp))
                            .id(String.valueOf(tuple.getT1()))
                            .event("message")
                            .build();
                });
    }

    private String joinPrompts(List<String> systemPrompts) {
        if (systemPrompts == null || systemPrompts.isEmpty()) {
            return "";
        }
        return String.join("\n", systemPrompts);
    }
}
