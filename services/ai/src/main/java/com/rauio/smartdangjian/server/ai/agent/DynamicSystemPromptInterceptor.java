package com.rauio.smartdangjian.server.ai.agent;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.PromptService;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.Map;

public class DynamicSystemPromptInterceptor extends ModelInterceptor {

    private final AiAgentType agentType;
    private final PromptService promptService;
    private final AiMemoryService aiMemoryService;

    public DynamicSystemPromptInterceptor(AiAgentType agentType,
                                          PromptService promptService,
                                          AiMemoryService aiMemoryService) {
        this.agentType = agentType;
        this.promptService = promptService;
        this.aiMemoryService = aiMemoryService;
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        Map<String, Object> context = request.getContext();
        String userId = context.get("userId") == null ? null : context.get("userId").toString();
        String sessionId = context.get("sessionId") == null ? null : context.get("sessionId").toString();

        StringBuilder promptBuilder = new StringBuilder(promptService.buildSystemPrompt(agentType.code()));
        String memory = aiMemoryService.buildLongTermMemory(userId, sessionId, 12);
        if (!memory.isBlank()) {
            promptBuilder.append("\n\n## Long Term Memory\n").append(memory);
        }

        SystemMessage existing = request.getSystemMessage();
        String finalPrompt = existing == null || existing.getText() == null || existing.getText().isBlank()
                ? promptBuilder.toString()
                : existing.getText() + "\n\n" + promptBuilder;

        return handler.call(ModelRequest.builder(request)
                .systemMessage(new SystemMessage(finalPrompt))
                .build());
    }

    @Override
    public String getName() {
        return "dynamic-system-prompt-" + agentType.code().toLowerCase();
    }
}
