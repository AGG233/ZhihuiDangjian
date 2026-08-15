package com.rauio.smartdangjian.server.search.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.config.RedisConfig;

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
                .weakChapterIds(List.of(1L))
                .build();

        UserProfileResponse.QuizStats quiz = UserProfileResponse.QuizStats.builder()
                .totalAnswers(50)
                .correctCount(42)
                .correctRate(0.84)
                .avgTimeSpent(45.6)
                .byDifficulty(Map.of("easy", 0.9, "medium", 0.75))
                .build();

        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .learning(learning)
                .knowledge(knowledge)
                .interestCategoryIds(List.of(1L, 2L))
                .quiz(quiz)
                .build();

        assertThat(profile.getUserId()).isEqualTo("1");
        assertThat(profile.getLearning().getTotalDuration()).isEqualTo(3600);
        assertThat(profile.getLearning().getPreferredDevice()).isEqualTo("web");
        assertThat(profile.getKnowledge().getAvgProgress()).isEqualTo(75.0);
        assertThat(profile.getKnowledge().getWeakChapterIds()).containsExactly(1L);
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
        UserProfileResponse.QuizStats stats =
                UserProfileResponse.QuizStats.builder().build();

        assertThat(stats.getTotalAnswers()).isZero();
        assertThat(stats.getCorrectCount()).isZero();
        assertThat(stats.getCorrectRate()).isZero();
        assertThat(stats.getAvgTimeSpent()).isZero();
        assertThat(stats.getByDifficulty()).isNull();
    }

    @Test
    @DisplayName("InteractionStats 构建与默认值（互动表现维度）")
    void interactionStatsBuildAndDefaults() {
        UserProfileResponse.InteractionStats stats = UserProfileResponse.InteractionStats.builder()
                .commentCount(5)
                .likeGivenCount(8)
                .activeWeeks(3)
                .build();

        assertThat(stats.getCommentCount()).isEqualTo(5L);
        assertThat(stats.getLikeGivenCount()).isEqualTo(8L);
        assertThat(stats.getActiveWeeks()).isEqualTo(3L);

        UserProfileResponse.InteractionStats empty =
                UserProfileResponse.InteractionStats.builder().build();
        assertThat(empty.getCommentCount()).isZero();
        assertThat(empty.getActiveWeeks()).isZero();
    }

    @Test
    @DisplayName("序列化 round-trip：含非空 InteractionStats 的画像经缓存序列化后类型与字段保留")
    void interactionStatsSerializationRoundTrip() throws JsonProcessingException {
        UserProfileResponse.InteractionStats interaction = UserProfileResponse.InteractionStats.builder()
                .commentCount(5L)
                .likeGivenCount(8L)
                .activeWeeks(3L)
                .build();

        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .interaction(interaction)
                .build();

        ObjectMapper mapper = RedisConfig.createCacheObjectMapper();
        String json = mapper.writeValueAsString(profile);
        UserProfileResponse restored = mapper.readValue(json, UserProfileResponse.class);

        assertThat(restored).isNotNull();
        assertThat(restored.getInteraction()).isNotNull();
        assertThat(restored.getInteraction()).isInstanceOf(UserProfileResponse.InteractionStats.class);
        assertThat(restored.getInteraction().getCommentCount()).isEqualTo(5L);
        assertThat(restored.getInteraction().getLikeGivenCount()).isEqualTo(8L);
        assertThat(restored.getInteraction().getActiveWeeks()).isEqualTo(3L);
        assertThat(restored.getInteraction()).isEqualTo(interaction);
    }
}
