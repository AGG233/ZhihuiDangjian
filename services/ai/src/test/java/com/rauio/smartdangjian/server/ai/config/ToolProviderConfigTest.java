package com.rauio.smartdangjian.server.ai.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.tool.ToolCallbackProvider;

import com.rauio.smartdangjian.server.ai.tool.AiQuizGeneratorTool;
import com.rauio.smartdangjian.server.ai.tool.ArticleDetailTool;
import com.rauio.smartdangjian.server.ai.tool.ContentReviewTool;
import com.rauio.smartdangjian.server.ai.tool.ContentSafetyTool;
import com.rauio.smartdangjian.server.ai.tool.ContentSearchTool;
import com.rauio.smartdangjian.server.ai.tool.LearningPathTool;
import com.rauio.smartdangjian.server.ai.tool.LearningTool;
import com.rauio.smartdangjian.server.ai.tool.QuizManageTool;
import com.rauio.smartdangjian.server.ai.tool.QuizTool;
import com.rauio.smartdangjian.server.ai.tool.RagSearchTool;
import com.rauio.smartdangjian.server.ai.tool.RecommendTool;
import com.rauio.smartdangjian.server.ai.tool.UserInfoTool;
import com.rauio.smartdangjian.server.ai.tool.UserProfileTool;
import com.rauio.smartdangjian.server.ai.tool.UserQuizAnswerTool;

class ToolProviderConfigTest {

    private final ToolProviderConfig config = new ToolProviderConfig();

    @ParameterizedTest
    @ValueSource(
            strings = {
                "userInfoToolProvider",
                "learningToolProvider",
                "userQuizAnswerToolProvider",
                "quizToolProvider",
                "recommendToolProvider",
                "userProfileToolProvider",
                "quizManageToolProvider",
                "contentSearchToolProvider",
                "ragSearchToolProvider",
                "aiQuizGeneratorToolProvider",
                "articleDetailToolProvider",
                "contentReviewToolProvider",
                "contentSafetyToolProvider",
                "learningPathToolProvider"
            })
    @DisplayName("所有 ToolCallbackProvider @Bean 方法应返回非空对象")
    void allProvidersAreCreated(String methodName) {
        ToolCallbackProvider provider =
                switch (methodName) {
                    case "userInfoToolProvider" -> config.userInfoToolProvider(mock(UserInfoTool.class));
                    case "learningToolProvider" -> config.learningToolProvider(mock(LearningTool.class));
                    case "userQuizAnswerToolProvider" ->
                        config.userQuizAnswerToolProvider(mock(UserQuizAnswerTool.class));
                    case "quizToolProvider" -> config.quizToolProvider(mock(QuizTool.class));
                    case "recommendToolProvider" -> config.recommendToolProvider(mock(RecommendTool.class));
                    case "userProfileToolProvider" -> config.userProfileToolProvider(mock(UserProfileTool.class));
                    case "quizManageToolProvider" -> config.quizManageToolProvider(mock(QuizManageTool.class));
                    case "contentSearchToolProvider" -> config.contentSearchToolProvider(mock(ContentSearchTool.class));
                    case "ragSearchToolProvider" -> config.ragSearchToolProvider(mock(RagSearchTool.class));
                    case "aiQuizGeneratorToolProvider" ->
                        config.aiQuizGeneratorToolProvider(mock(AiQuizGeneratorTool.class));
                    case "articleDetailToolProvider" -> config.articleDetailToolProvider(mock(ArticleDetailTool.class));
                    case "contentReviewToolProvider" -> config.contentReviewToolProvider(mock(ContentReviewTool.class));
                    case "contentSafetyToolProvider" -> config.contentSafetyToolProvider(mock(ContentSafetyTool.class));
                    case "learningPathToolProvider" -> config.learningPathToolProvider(mock(LearningPathTool.class));
                    default -> throw new IllegalArgumentException("Unknown provider: " + methodName);
                };
        assertThat(provider)
                .as("@Bean '%s' 应返回非空 ToolCallbackProvider", methodName)
                .isNotNull();
    }
}
