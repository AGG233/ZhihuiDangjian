package com.rauio.smartdangjian.server.search.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserProfileResponse 用户画像视图对象")
class UserProfileResponseTest {

    @Test
    @DisplayName("使用 builder 构造完整画像")
    void buildCompleteProfile() {
        UserProfileResponse.LearningStats learning = UserProfileResponse.LearningStats.builder()
                .totalDuration(3600)
                .avgDuration(600)
                .totalRecords(6)
                .completedChapters(4)
                .preferredDevice("web")
                .build();

        UserProfileResponse.KnowledgeStats knowledge = UserProfileResponse.KnowledgeStats.builder()
                .avgProgress(75.0)
                .completionRate(0.66)
                .weakChapterIds(List.of("ch-weak"))
                .build();

        UserProfileResponse.QuizStats quiz = UserProfileResponse.QuizStats.builder()
                .totalAnswers(50)
                .correctCount(42)
                .correctRate(0.84)
                .avgTimeSpent(45.6)
                .byDifficulty(Map.of("easy", 0.9, "medium", 0.75))
                .build();

        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("user-1")
                .learning(learning)
                .knowledge(knowledge)
                .interestCategoryIds(List.of("cat-1", "cat-2"))
                .quiz(quiz)
                .build();

        assertThat(profile.getUserId()).isEqualTo("user-1");
        assertThat(profile.getLearning().getTotalDuration()).isEqualTo(3600);
        assertThat(profile.getLearning().getPreferredDevice()).isEqualTo("web");
        assertThat(profile.getKnowledge().getAvgProgress()).isEqualTo(75.0);
        assertThat(profile.getKnowledge().getWeakChapterIds()).containsExactly("ch-weak");
        assertThat(profile.getInterestCategoryIds()).hasSize(2);
        assertThat(profile.getQuiz().getCorrectRate()).isEqualTo(0.84);
        assertThat(profile.getQuiz().getByDifficulty()).containsKey("easy");
    }

    @Test
    @DisplayName("LearningStats 默认值检查")
    void learningStatsDefaults() {
        UserProfileResponse.LearningStats stats =
                UserProfileResponse.LearningStats.builder().build();

        assertThat(stats.getTotalDuration()).isZero();
        assertThat(stats.getAvgDuration()).isZero();
        assertThat(stats.getTotalRecords()).isZero();
        assertThat(stats.getCompletedChapters()).isZero();
        assertThat(stats.getPreferredDevice()).isNull();
    }

    @Test
    @DisplayName("KnowledgeStats 默认值")
    void knowledgeStatsDefaults() {
        UserProfileResponse.KnowledgeStats stats =
                UserProfileResponse.KnowledgeStats.builder().build();

        assertThat(stats.getAvgProgress()).isZero();
        assertThat(stats.getCompletionRate()).isZero();
        assertThat(stats.getWeakChapterIds()).isNull();
    }

    @Test
    @DisplayName("QuizStats 默认值")
    void quizStatsDefaults() {
        UserProfileResponse.QuizStats stats = UserProfileResponse.QuizStats.builder().build();

        assertThat(stats.getTotalAnswers()).isZero();
        assertThat(stats.getCorrectCount()).isZero();
        assertThat(stats.getCorrectRate()).isZero();
        assertThat(stats.getAvgTimeSpent()).isZero();
        assertThat(stats.getByDifficulty()).isNull();
    }
}
