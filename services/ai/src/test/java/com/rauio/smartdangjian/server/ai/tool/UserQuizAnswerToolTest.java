package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.quiz.api.UserQuizQueryFacade;
import com.rauio.smartdangjian.server.quiz.pojo.dto.UserQuizAnswerDto;

@ExtendWith(MockitoExtension.class)
class UserQuizAnswerToolTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private UserQuizQueryFacade userQuizQueryFacade;

    @InjectMocks
    private UserQuizAnswerTool userQuizAnswerTool;

    private UserQuizAnswerDto createAnswer(LocalDateTime answerTime) {
        return UserQuizAnswerDto.builder()
                .id(1L)
                .userId(1L)
                .quizId(1L)
                .optionId(1L)
                .isCorrect(1)
                .timeSpent(10)
                .answerTime(answerTime)
                .build();
    }

    @Test
    @DisplayName("getRecentQuizAnswers 返回最近答题记录（按时间倒序）")
    void getRecentQuizAnswers() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");

        UserQuizAnswerDto answer1 = createAnswer(LocalDateTime.now().minusDays(2));
        UserQuizAnswerDto answer2 = createAnswer(LocalDateTime.now());

        when(userQuizQueryFacade.listByUserId(1L)).thenReturn(List.of(answer1, answer2));

        List<UserQuizAnswerDto> result = userQuizAnswerTool.getRecentQuizAnswers(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(answer2);
        assertThat(result.get(1)).isEqualTo(answer1);
    }

    @Test
    @DisplayName("getRecentQuizAnswers limit 为 null 时默认返回 10 条")
    void getRecentQuizAnswersDefaultLimit() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");

        when(userQuizQueryFacade.listByUserId(1L)).thenReturn(List.of());

        List<UserQuizAnswerDto> result = userQuizAnswerTool.getRecentQuizAnswers(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRecentQuizAnswers with limit=0 defaults to 10")
    void getRecentQuizAnswersZeroLimit() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");

        when(userQuizQueryFacade.listByUserId(1L)).thenReturn(List.of());

        List<UserQuizAnswerDto> result = userQuizAnswerTool.getRecentQuizAnswers(0);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRecentQuizAnswers with negative limit defaults to 10")
    void getRecentQuizAnswersNegativeLimit() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");

        when(userQuizQueryFacade.listByUserId(1L)).thenReturn(List.of());

        List<UserQuizAnswerDto> result = userQuizAnswerTool.getRecentQuizAnswers(-5);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getQuizAnswersByQuizId returns answers for quiz")
    void getQuizAnswersByQuizId() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");

        UserQuizAnswerDto answer = createAnswer(LocalDateTime.now());
        when(userQuizQueryFacade.listByUserIdAndQuizId(1L, 1L)).thenReturn(List.of(answer));

        List<UserQuizAnswerDto> result = userQuizAnswerTool.getQuizAnswersByQuizId("1");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getQuizAnswersByQuizId quizId 非数字时抛出参数异常")
    void getQuizAnswersByQuizIdInvalidQuizId() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");

        assertThatThrownBy(() -> userQuizAnswerTool.getQuizAnswersByQuizId("not-a-number"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getRecentQuizAnswers 当前用户缺失时返回空列表")
    void getRecentQuizAnswersWithMissingCurrentUser() {
        List<UserQuizAnswerDto> result = userQuizAnswerTool.getRecentQuizAnswers(5);

        assertThat(result).isEmpty();
    }
}
