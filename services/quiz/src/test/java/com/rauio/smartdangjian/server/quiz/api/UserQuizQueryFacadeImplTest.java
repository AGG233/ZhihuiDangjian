package com.rauio.smartdangjian.server.quiz.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.quiz.pojo.dto.UserQuizAnswerDto;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserQuizQueryFacadeImpl")
class UserQuizQueryFacadeImplTest {

    @Mock
    private UserQuizAnswerService userQuizAnswerService;

    @Mock
    private QuizService quizService;

    @InjectMocks
    private UserQuizQueryFacadeImpl facade;

    private UserQuizAnswer sampleAnswer() {
        return UserQuizAnswer.builder()
                .id(1L)
                .userId(100L)
                .quizId(200L)
                .optionId(300L)
                .isCorrect(1)
                .timeSpent(30)
                .answerTime(LocalDateTime.of(2026, 6, 1, 10, 0))
                .build();
    }

    @Nested
    @DisplayName("listByUserId 方法")
    class ListByUserId {

        @Test
        @DisplayName("调用 service 的 getByUserId 并转换返回 DTO 列表")
        void delegatesAndConverts() {
            List<UserQuizAnswer> records = List.of(sampleAnswer());
            when(userQuizAnswerService.getByUserId(100L)).thenReturn(records);

            List<UserQuizAnswerDto> result = facade.listByUserId(100L);

            assertThat(result).hasSize(1);
            UserQuizAnswerDto dto = result.get(0);
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getUserId()).isEqualTo(100L);
            assertThat(dto.getQuizId()).isEqualTo(200L);
            assertThat(dto.getOptionId()).isEqualTo(300L);
            assertThat(dto.getIsCorrect()).isEqualTo(1);
            assertThat(dto.getTimeSpent()).isEqualTo(30);
            assertThat(dto.getAnswerTime()).isEqualTo(LocalDateTime.of(2026, 6, 1, 10, 0));
        }

        @Test
        @DisplayName("service 返回空列表时返回空列表")
        void emptyRecordsReturnsEmptyList() {
            when(userQuizAnswerService.getByUserId(100L)).thenReturn(Collections.emptyList());

            List<UserQuizAnswerDto> result = facade.listByUserId(100L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("service 返回 null 时返回空列表")
        void nullRecordsReturnsEmptyList() {
            when(userQuizAnswerService.getByUserId(100L)).thenReturn(null);

            List<UserQuizAnswerDto> result = facade.listByUserId(100L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listByUserIdAndQuizId 方法")
    class ListByUserIdAndQuizId {

        @Test
        @DisplayName("调用 service 的 getByUserIdAndQuizId 并转换返回 DTO 列表")
        void delegatesAndConverts() {
            List<UserQuizAnswer> records = List.of(sampleAnswer());
            when(userQuizAnswerService.getByUserIdAndQuizId(100L, 200L)).thenReturn(records);

            List<UserQuizAnswerDto> result = facade.listByUserIdAndQuizId(100L, 200L);

            assertThat(result).hasSize(1);
            UserQuizAnswerDto dto = result.get(0);
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getUserId()).isEqualTo(100L);
            assertThat(dto.getQuizId()).isEqualTo(200L);
            assertThat(dto.getOptionId()).isEqualTo(300L);
            assertThat(dto.getIsCorrect()).isEqualTo(1);
            assertThat(dto.getTimeSpent()).isEqualTo(30);
            assertThat(dto.getAnswerTime()).isEqualTo(LocalDateTime.of(2026, 6, 1, 10, 0));
        }

        @Test
        @DisplayName("service 返回空列表时返回空列表")
        void emptyRecordsReturnsEmptyList() {
            when(userQuizAnswerService.getByUserIdAndQuizId(100L, 200L)).thenReturn(Collections.emptyList());

            List<UserQuizAnswerDto> result = facade.listByUserIdAndQuizId(100L, 200L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("service 返回 null 时返回空列表")
        void nullRecordsReturnsEmptyList() {
            when(userQuizAnswerService.getByUserIdAndQuizId(100L, 200L)).thenReturn(null);

            List<UserQuizAnswerDto> result = facade.listByUserIdAndQuizId(100L, 200L);

            assertThat(result).isEmpty();
        }
    }
}
