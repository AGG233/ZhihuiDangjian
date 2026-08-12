package com.rauio.smartdangjian.server.ai.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rauio.smartdangjian.server.ai.tool.AiQuizGeneratorTool;
import com.rauio.smartdangjian.server.ai.tool.ArticleDetailTool;
import com.rauio.smartdangjian.server.ai.tool.ContentReviewTool;
import com.rauio.smartdangjian.server.ai.tool.ContentSafetyTool;
import com.rauio.smartdangjian.server.ai.tool.ContentSearchTool;
import com.rauio.smartdangjian.server.ai.tool.LearningPathTool;
import com.rauio.smartdangjian.server.ai.tool.LearningTool;
import com.rauio.smartdangjian.server.ai.tool.QuizManageTool;
import com.rauio.smartdangjian.server.ai.tool.QuizTool;
import com.rauio.smartdangjian.server.ai.tool.RecommendTool;
import com.rauio.smartdangjian.server.ai.tool.UserInfoTool;
import com.rauio.smartdangjian.server.ai.tool.UserProfileTool;
import com.rauio.smartdangjian.server.ai.tool.UserQuizAnswerTool;

@Configuration
public class ToolProviderConfig {

    @Bean
    public ToolCallbackProvider userInfoToolProvider(UserInfoTool userInfoTool) {
        return MethodToolCallbackProvider.builder().toolObjects(userInfoTool).build();
    }

    @Bean
    public ToolCallbackProvider learningToolProvider(LearningTool learningTool) {
        return MethodToolCallbackProvider.builder().toolObjects(learningTool).build();
    }

    @Bean
    public ToolCallbackProvider userQuizAnswerToolProvider(UserQuizAnswerTool userQuizAnswerTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(userQuizAnswerTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider quizToolProvider(QuizTool quizTool) {
        return MethodToolCallbackProvider.builder().toolObjects(quizTool).build();
    }

    @Bean
    public ToolCallbackProvider recommendToolProvider(RecommendTool recommendTool) {
        return MethodToolCallbackProvider.builder().toolObjects(recommendTool).build();
    }

    @Bean
    public ToolCallbackProvider userProfileToolProvider(UserProfileTool userProfileTool) {
        return MethodToolCallbackProvider.builder().toolObjects(userProfileTool).build();
    }

    @Bean
    public ToolCallbackProvider quizManageToolProvider(QuizManageTool quizManageTool) {
        return MethodToolCallbackProvider.builder().toolObjects(quizManageTool).build();
    }

    @Bean
    public ToolCallbackProvider contentSearchToolProvider(ContentSearchTool contentSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(contentSearchTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider aiQuizGeneratorToolProvider(AiQuizGeneratorTool aiQuizGeneratorTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(aiQuizGeneratorTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider articleDetailToolProvider(ArticleDetailTool articleDetailTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(articleDetailTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider contentReviewToolProvider(ContentReviewTool contentReviewTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(contentReviewTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider contentSafetyToolProvider(ContentSafetyTool contentSafetyTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(contentSafetyTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider learningPathToolProvider(LearningPathTool learningPathTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(learningPathTool)
                .build();
    }
}
