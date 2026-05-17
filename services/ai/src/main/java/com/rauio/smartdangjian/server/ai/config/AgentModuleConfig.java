package com.rauio.smartdangjian.server.ai.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AgentModuleConfig {

    private static final String ROUTING_SYSTEM_PROMPT = """
            你是智慧党建平台的智能路由协调者。你的职责是根据用户输入，从以下专业Agent中选择最合适的来处理请求。

            可选Agent：
            - study-assistant-agent: 党务学习日常问答、知识讲解、学习指导。当用户询问党建知识、理论、政策，或需要学习建议时使用。
            - content-discovery-agent: 课程/文章/章节搜索、内容浏览、推荐。当用户搜索课程、文章、章节，或询问有哪些学习资源时使用。
            - assessment-agent: 测试题目生成、答题评估、学习评估。当用户要求出题、测试、评估学习效果、查看答题记录时使用。
            - review-agent: 内容质量审查与用户内容安全审核。当用户要求审查课程/文章/题目的质量，或检查内容的合规性时使用。
            - profile-agent: 学习画像分析、学习路径规划、数据洞察。当用户想了解自己的学习情况、获取学习建议或个性化推荐时使用。

            根据用户输入的消息内容，选择最合适的Agent并返回其名称。如果问题涉及多个方面，选择最主要的那个。
            """;

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
                                           @Qualifier("userInfoToolProvider") ToolCallbackProvider userInfoToolProvider,
                                           @Qualifier("learningToolProvider") ToolCallbackProvider learningToolProvider,
                                           @Qualifier("userQuizAnswerToolProvider") ToolCallbackProvider userQuizAnswerToolProvider,
                                           @Qualifier("quizToolProvider") ToolCallbackProvider quizToolProvider,
                                           @Qualifier("recommendToolProvider") ToolCallbackProvider recommendToolProvider,
                                           @Qualifier("userProfileToolProvider") ToolCallbackProvider userProfileToolProvider,
                                           @Qualifier("quizManageToolProvider") ToolCallbackProvider quizManageToolProvider,
                                           @Qualifier("contentSearchToolProvider") ToolCallbackProvider contentSearchToolProvider,
                                           @Qualifier("aiQuizGeneratorToolProvider") ToolCallbackProvider aiQuizGeneratorToolProvider,
                                           @Qualifier("articleDetailToolProvider") ToolCallbackProvider articleDetailToolProvider,
                                           @Qualifier("contentReviewToolProvider") ToolCallbackProvider contentReviewToolProvider,
                                           @Qualifier("contentSafetyToolProvider") ToolCallbackProvider contentSafetyToolProvider,
                                           @Qualifier("learningPathToolProvider") ToolCallbackProvider learningPathToolProvider) {

        // 1. StudyAssistant Agent: 日常问答、知识讲解
        ReactAgent studyAssistant = ReactAgent.builder()
                .name(AiAgentType.STUDY_ASSISTANT.agentName())
                .description(AiAgentType.STUDY_ASSISTANT.description())
                .model(chatModel)
                .toolCallbackProviders(userInfoToolProvider, learningToolProvider,
                        recommendToolProvider, userProfileToolProvider)
                .saver(redisSaver)
                .hooks(skillsAgentHook)
                .interceptors(new DynamicSystemPromptInterceptor(
                        AiAgentType.STUDY_ASSISTANT, promptService, aiMemoryService))
                .parallelToolExecution(true)
                .maxParallelTools(4)
                .toolExecutionTimeout(Duration.ofSeconds(30))
                .build();

        // 2. ContentDiscovery Agent: 内容搜索、浏览、推荐
        ReactAgent contentDiscovery = ReactAgent.builder()
                .name(AiAgentType.CONTENT_DISCOVERY.agentName())
                .description(AiAgentType.CONTENT_DISCOVERY.description())
                .model(chatModel)
                .toolCallbackProviders(contentSearchToolProvider, recommendToolProvider,
                        articleDetailToolProvider)
                .saver(redisSaver)
                .hooks(skillsAgentHook)
                .interceptors(new DynamicSystemPromptInterceptor(
                        AiAgentType.CONTENT_DISCOVERY, promptService, aiMemoryService))
                .parallelToolExecution(true)
                .maxParallelTools(4)
                .toolExecutionTimeout(Duration.ofSeconds(30))
                .build();

        // 3. Assessment Agent: 题目生成、答题评估
        ReactAgent assessment = ReactAgent.builder()
                .name(AiAgentType.ASSESSMENT.agentName())
                .description(AiAgentType.ASSESSMENT.description())
                .model(chatModel)
                .toolCallbackProviders(quizManageToolProvider, aiQuizGeneratorToolProvider,
                        contentSearchToolProvider, quizToolProvider,
                        learningToolProvider, userQuizAnswerToolProvider, userProfileToolProvider)
                .saver(redisSaver)
                .hooks(skillsAgentHook)
                .interceptors(new DynamicSystemPromptInterceptor(
                        AiAgentType.ASSESSMENT, promptService, aiMemoryService))
                .parallelToolExecution(true)
                .maxParallelTools(4)
                .toolExecutionTimeout(Duration.ofSeconds(30))
                .build();

        // 4. Review Agent: 内容审查
        ReactAgent review = ReactAgent.builder()
                .name(AiAgentType.REVIEW.agentName())
                .description(AiAgentType.REVIEW.description())
                .model(chatModel)
                .toolCallbackProviders(contentSearchToolProvider, quizManageToolProvider,
                        articleDetailToolProvider, contentReviewToolProvider, contentSafetyToolProvider)
                .saver(redisSaver)
                .hooks(skillsAgentHook)
                .interceptors(new DynamicSystemPromptInterceptor(
                        AiAgentType.REVIEW, promptService, aiMemoryService))
                .parallelToolExecution(true)
                .maxParallelTools(4)
                .toolExecutionTimeout(Duration.ofSeconds(30))
                .build();

        // 5. Profile Agent: 学习画像、路径规划
        ReactAgent profile = ReactAgent.builder()
                .name(AiAgentType.PROFILE.agentName())
                .description(AiAgentType.PROFILE.description())
                .model(chatModel)
                .toolCallbackProviders(userProfileToolProvider, learningToolProvider,
                        userQuizAnswerToolProvider, userInfoToolProvider,
                        recommendToolProvider, learningPathToolProvider)
                .saver(redisSaver)
                .hooks(skillsAgentHook)
                .interceptors(new DynamicSystemPromptInterceptor(
                        AiAgentType.PROFILE, promptService, aiMemoryService))
                .parallelToolExecution(true)
                .maxParallelTools(4)
                .toolExecutionTimeout(Duration.ofSeconds(30))
                .build();

        // 6. Coordinator (LlmRoutingAgent): 路由分发
        LlmRoutingAgent coordinator = LlmRoutingAgent.builder()
                .name("coordinator-agent")
                .description("智能路由协调者")
                .model(chatModel)
                .systemPrompt(ROUTING_SYSTEM_PROMPT)
                .subAgents(List.of(studyAssistant, contentDiscovery, assessment, review, profile))
                .saver(redisSaver)
                .build();

        Map<AiAgentType, ReactAgent> specialistMap = new EnumMap<>(AiAgentType.class);
        specialistMap.put(AiAgentType.STUDY_ASSISTANT, studyAssistant);
        specialistMap.put(AiAgentType.CONTENT_DISCOVERY, contentDiscovery);
        specialistMap.put(AiAgentType.ASSESSMENT, assessment);
        specialistMap.put(AiAgentType.REVIEW, review);
        specialistMap.put(AiAgentType.PROFILE, profile);

        return new AiAgentRegistry(coordinator, specialistMap);
    }
}
