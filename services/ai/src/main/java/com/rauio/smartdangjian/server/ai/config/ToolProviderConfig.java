package com.rauio.smartdangjian.server.ai.config;

import com.rauio.smartdangjian.server.ai.tool.LearningTool;
import com.rauio.smartdangjian.server.ai.tool.QuizTool;
import com.rauio.smartdangjian.server.ai.tool.UserInfoTool;
import com.rauio.smartdangjian.server.ai.tool.UserQuizAnswerTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolProviderConfig {

    @Bean
    public ToolCallbackProvider userInfoToolProvider(UserInfoTool userInfoTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(userInfoTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider learningToolProvider(LearningTool learningTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(learningTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider userQuizAnswerToolProvider(UserQuizAnswerTool userQuizAnswerTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(userQuizAnswerTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider quizToolProvider(QuizTool quizTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(quizTool)
                .build();
    }
}
