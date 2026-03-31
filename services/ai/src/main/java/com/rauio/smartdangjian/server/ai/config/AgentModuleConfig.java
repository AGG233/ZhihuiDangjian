package com.rauio.smartdangjian.server.ai.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.rauio.smartdangjian.server.ai.agent.AiAgentRegistry;
import com.rauio.smartdangjian.server.ai.agent.AiAgentType;
import com.rauio.smartdangjian.server.ai.agent.DatabaseSkillRegistry;
import com.rauio.smartdangjian.server.ai.agent.DynamicSystemPromptInterceptor;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.PromptService;
import com.rauio.smartdangjian.server.ai.service.SkillService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AgentModuleConfig {

    @Bean
    public SkillRegistry skillRegistry(SkillService skillService) {
        return new DatabaseSkillRegistry(skillService);
    }

    @Bean
    public SkillsAgentHook skillsAgentHook(SkillRegistry skillRegistry) {
        return SkillsAgentHook.builder()
                .skillRegistry(skillRegistry)
                .autoReload(true)
                .build();
    }

    @Bean
    public AiAgentRegistry aiAgentRegistry(ChatModel chatModel,
                                           RedisSaver redisSaver,
                                           PromptService promptService,
                                           AiMemoryService aiMemoryService,
                                           SkillsAgentHook skillsAgentHook,
                                           List<ToolCallbackProvider> toolCallbackProviders) {
        Map<AiAgentType, ReactAgent> agentMap = new EnumMap<>(AiAgentType.class);
        for (AiAgentType type : AiAgentType.values()) {
            agentMap.put(type, ReactAgent.builder()
                    .name(type.agentName())
                    .description(type.description())
                    .model(chatModel)
                    .systemPrompt("你是智慧党建平台AI助手，必须基于系统提示词、技能和记忆上下文作答。")
                    .toolCallbackProviders(toolCallbackProviders.toArray(ToolCallbackProvider[]::new))
                    .saver(redisSaver)
                    .hooks(skillsAgentHook)
                    .interceptors(new DynamicSystemPromptInterceptor(type, promptService, aiMemoryService))
                    .parallelToolExecution(true)
                    .maxParallelTools(4)
                    .toolExecutionTimeout(Duration.ofSeconds(30))
                    .build());
        }
        return new AiAgentRegistry(agentMap);
    }
}
