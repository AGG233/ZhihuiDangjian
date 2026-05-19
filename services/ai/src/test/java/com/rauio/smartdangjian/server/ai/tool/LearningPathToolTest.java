package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.search.pojo.vo.UserProfileVO;
import com.rauio.smartdangjian.search.service.UserProfileService;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        UserProfileVO.LearningStats learning = UserProfileVO.LearningStats.builder()
                .totalDuration(3600)
                .totalRecords(12)
                .completedChapters(8)
                .build();

        UserProfileVO.KnowledgeStats knowledge = UserProfileVO.KnowledgeStats.builder()
                .avgProgress(76.5)
                .completionRate(0.8)
                .build();

        UserProfileVO.QuizStats quiz = UserProfileVO.QuizStats.builder()
                .totalAnswers(50)
                .correctCount(42)
                .correctRate(0.84)
                .build();

        UserProfileVO profile = UserProfileVO.builder()
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
