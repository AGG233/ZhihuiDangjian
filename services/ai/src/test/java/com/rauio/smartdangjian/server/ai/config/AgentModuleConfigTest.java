package com.rauio.smartdangjian.server.ai.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.rauio.smartdangjian.server.ai.agent.AiAgentRegistry;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.PromptService;
import com.rauio.smartdangjian.server.ai.service.SkillService;

class AgentModuleConfigTest {

    private final AgentModuleConfig config = new AgentModuleConfig();

    @Test
    @DisplayName("skillRegistry @Bean 应返回非空 SkillRegistry")
    void skillRegistry() {
        var skillService = mock(SkillService.class);
        SkillRegistry registry = config.skillRegistry(skillService);
        assertThat(registry).isNotNull();
    }

    @Test
    @DisplayName("skillsAgentHook @Bean 应返回非空 SkillsAgentHook")
    void skillsAgentHook() {
        var skillRegistry = mock(SkillRegistry.class);
        SkillsAgentHook hook = config.skillsAgentHook(skillRegistry);
        assertThat(hook).isNotNull();
    }

    @Test
    @DisplayName("aiAgentRegistry @Bean 应返回非空 AiAgentRegistry")
    void aiAgentRegistry() {
        var chatModel = mock(ChatModel.class);
        var redisSaver = mock(RedisSaver.class);
        var promptService = mock(PromptService.class);
        var aiMemoryService = mock(AiMemoryService.class);
        var skillsAgentHook = mock(SkillsAgentHook.class);
        var anyProvider = mock(ToolCallbackProvider.class);
        when(anyProvider.getToolCallbacks()).thenReturn(new ToolCallback[0]);

        AiAgentRegistry registry = config.aiAgentRegistry(
                chatModel,
                redisSaver,
                promptService,
                aiMemoryService,
                skillsAgentHook,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider,
                anyProvider);

        assertThat(registry).isNotNull();
    }
}
