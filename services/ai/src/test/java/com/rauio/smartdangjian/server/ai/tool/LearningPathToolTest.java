package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.search.api.SearchQueryFacade;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;

@ExtendWith(MockitoExtension.class)
class LearningPathToolTest {

    @Mock
    private SearchQueryFacade searchQueryFacade;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private LearningPathTool learningPathTool;

    @Test
    @DisplayName("getLearningProfile 返回用户学习画像数据")
    void getLearningProfile() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
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
                .userId("1")
                .learning(learning)
                .knowledge(knowledge)
                .interestCategoryIds(List.of(1L, 2L))
                .quiz(quiz)
                .build();

        when(searchQueryFacade.getProfile("1")).thenReturn(profile);

        Map<String, Object> result = learningPathTool.getLearningProfile();

        assertThat(result).containsKey("learningStats");
        assertThat(result).containsKey("knowledgeStats");
        assertThat(result).containsKey("interestCategoryIds");
        assertThat(result).containsKey("quizStats");
    }

    @Test
    @DisplayName("getLearningProfile profile 为 null 时返回空 Map")
    void getLearningProfileNull() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        when(searchQueryFacade.getProfile("1")).thenReturn(null);

        Map<String, Object> result = learningPathTool.getLearningProfile();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getLearningProfile profile 的子字段为 null 时 Map 值对应为 null")
    void getLearningProfilePartialData() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .learning(null)
                .knowledge(null)
                .interestCategoryIds(null)
                .quiz(null)
                .build();

        when(searchQueryFacade.getProfile("1")).thenReturn(profile);

        Map<String, Object> result = learningPathTool.getLearningProfile();

        assertThat(result).containsKey("learningStats");
        assertThat(result).containsKey("knowledgeStats");
        assertThat(result).containsKey("interestCategoryIds");
        assertThat(result).containsKey("quizStats");
        assertThat(result.get("learningStats")).isNull();
        assertThat(result.get("interestCategoryIds")).isNull();
    }

    @Test
    @DisplayName("getLearningProfile profile 的兴趣分类为空列表时返回空列表")
    void getLearningProfileEmptyInterestList() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        UserProfileResponse.LearningStats learning = UserProfileResponse.LearningStats.builder()
                .totalDuration(100)
                .totalRecords(1)
                .completedChapters(0)
                .build();

        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .learning(learning)
                .knowledge(null)
                .interestCategoryIds(List.of())
                .quiz(null)
                .build();

        when(searchQueryFacade.getProfile("1")).thenReturn(profile);

        Map<String, Object> result = learningPathTool.getLearningProfile();

        assertThat(result).containsKey("interestCategoryIds");
        assertThat(result.get("interestCategoryIds")).isInstanceOf(List.class);
        assertThat((List<?>) result.get("interestCategoryIds")).isEmpty();
    }

    @Test
    @DisplayName("getLearningProfile 当前用户 ID 为 null 时以 null 查询并返回空 Map")
    void getLearningProfileWithNullUserId() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(null);
        when(searchQueryFacade.getProfile(null)).thenReturn(null);

        Map<String, Object> result = learningPathTool.getLearningProfile();

        assertThat(result).isEmpty();
    }
}
