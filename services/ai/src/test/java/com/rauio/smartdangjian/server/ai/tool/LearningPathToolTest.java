package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.search.service.UserProfileService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class LearningPathToolTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LearningPathTool learningPathTool;

    @Test
    @DisplayName("getLearningProfile 返回用户学习画像数据")
    void getLearningProfile() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");

        UserProfileResponse.LearningStats learning = UserProfileResponse.LearningStats.builder()
                .totalDuration(3600)
                .totalRecords(12)
                .completedChapters(8)
                .build();

        UserProfileResponse.KnowledgeStats knowledge = UserProfileResponse.KnowledgeStats.builder()
                .avgProgress(76.5)
                .completionRate(0.8)
                .build();

        UserProfileResponse.QuizStats quiz = UserProfileResponse.QuizStats.builder()
                .totalAnswers(50)
                .correctCount(42)
                .correctRate(0.84)
                .build();

        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("user-1")
                .learning(learning)
                .knowledge(knowledge)
                .interestCategoryIds(List.of("cat-1", "cat-2"))
                .quiz(quiz)
                .build();

        when(userProfileService.getProfile("user-1")).thenReturn(profile);

        Map<String, Object> result = learningPathTool.getLearningProfile(toolContext);

        assertThat(result).containsKey("learningStats");
        assertThat(result).containsKey("knowledgeStats");
        assertThat(result).containsKey("interestCategoryIds");
        assertThat(result).containsKey("quizStats");
    }

    @Test
    @DisplayName("getLearningProfile profile 为 null 时返回空 Map")
    void getLearningProfileNull() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");
        when(userProfileService.getProfile("user-1")).thenReturn(null);

        Map<String, Object> result = learningPathTool.getLearningProfile(toolContext);

        assertThat(result).isEmpty();
    }
}
