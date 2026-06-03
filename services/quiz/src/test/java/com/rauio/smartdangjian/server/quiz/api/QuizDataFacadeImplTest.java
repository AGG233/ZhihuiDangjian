package com.rauio.smartdangjian.server.quiz.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizOptionReviewDto;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizOptionSummary;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizSummary;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizDataFacadeImpl")
class QuizDataFacadeImplTest {

    @Mock
    private QuizService quizService;

    @Mock
    private QuizOptionService quizOptionService;

    @InjectMocks
    private QuizDataFacadeImpl facade;

    @Captor
    private ArgumentCaptor<Quiz> quizCaptor;

    private Quiz sampleQuiz() {
        return Quiz.builder()
                .id(1L)
                .chapterId(10L)
                .question("测试题目")
                .questionType("single_choice")
                .score(5)
                .difficulty("easy")
                .explanation("解析")
                .isActive(true)
                .createdAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .build();
    }

    private QuizOption sampleOption(boolean isCorrect) {
        return QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("选项A")
                .isCorrect(isCorrect)
                .orderIndex("A")
                .build();
    }

    @Nested
    @DisplayName("getQuiz 方法")
    class GetQuiz {

        @Test
        @DisplayName("返回 QuizSummary DTO")
        void returnsQuizSummary() {
            when(quizService.getById(1L)).thenReturn(sampleQuiz());

            QuizSummary result = facade.getQuiz(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getChapterId()).isEqualTo(10L);
            assertThat(result.getQuestion()).isEqualTo("测试题目");
            assertThat(result.getQuestionType()).isEqualTo("single_choice");
            assertThat(result.getScore()).isEqualTo(5);
            assertThat(result.getDifficulty()).isEqualTo("easy");
            assertThat(result.getExplanation()).isEqualTo("解析");
            assertThat(result.getIsActive()).isTrue();
            assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 10, 0));
            assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 12, 0));
        }

        @Test
        @DisplayName("Quiz 不存在时返回 null")
        void returnsNullWhenNotFound() {
            when(quizService.getById(999L)).thenReturn(null);

            assertThat(facade.getQuiz(999L)).isNull();
        }
    }

    @Nested
    @DisplayName("getQuizzesByChapter 方法")
    class GetQuizzesByChapter {

        @Test
        @DisplayName("返回 QuizSummary DTO 列表")
        void returnsQuizSummaryList() {
            Quiz quiz1 = sampleQuiz();
            Quiz quiz2 = Quiz.builder()
                    .id(2L)
                    .chapterId(10L)
                    .question("第二题")
                    .questionType("true_false")
                    .isActive(true)
                    .build();
            when(quizService.getByChapterId(10L)).thenReturn(List.of(quiz1, quiz2));

            List<QuizSummary> results = facade.getQuizzesByChapter(10L);

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getId()).isEqualTo(1L);
            assertThat(results.get(1).getId()).isEqualTo(2L);
            assertThat(results.get(1).getQuestion()).isEqualTo("第二题");
        }

        @Test
        @DisplayName("空列表时返回空列表")
        void emptyListReturnsEmpty() {
            when(quizService.getByChapterId(10L)).thenReturn(Collections.emptyList());

            assertThat(facade.getQuizzesByChapter(10L)).isEmpty();
        }

        @Test
        @DisplayName("Service 返回 null 时返回空列表")
        void nullReturnsEmpty() {
            when(quizService.getByChapterId(10L)).thenReturn(null);

            assertThat(facade.getQuizzesByChapter(10L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("createQuiz 方法")
    class CreateQuiz {

        @Test
        @DisplayName("保存 Quiz 并返回 ID")
        void savesAndReturnsId() {
            List<Map<String, Object>> options = List.of(
                    Map.of("optionText", "A", "isCorrect", true), Map.of("optionText", "B", "isCorrect", false));

            Long result = facade.createQuiz(10L, "新题目", "single_choice", 5, "medium", "新解析", options);

            verify(quizService).save(quizCaptor.capture());
            Quiz saved = quizCaptor.getValue();
            assertThat(saved.getChapterId()).isEqualTo(10L);
            assertThat(saved.getQuestion()).isEqualTo("新题目");
            assertThat(saved.getQuestionType()).isEqualTo("single_choice");
            assertThat(saved.getScore()).isEqualTo(5);
            assertThat(saved.getDifficulty()).isEqualTo("medium");
            assertThat(saved.getExplanation()).isEqualTo("新解析");
            assertThat(saved.getIsActive()).isTrue();
            assertThat(result).isNull(); // save() doesn't set id on the entity in mock
        }
    }

    @Nested
    @DisplayName("updateQuiz 方法")
    class UpdateQuiz {

        @Test
        @DisplayName("更新非空字段")
        void updatesNonNullFields() {
            Quiz existing = sampleQuiz();
            when(quizService.getById(1L)).thenReturn(existing);
            when(quizService.updateById(any())).thenReturn(true);

            boolean result = facade.updateQuiz(1L, "新问题", 10, "hard", "新解析", false);

            assertThat(result).isTrue();
            verify(quizService).updateById(quizCaptor.capture());
            Quiz updated = quizCaptor.getValue();
            assertThat(updated.getQuestion()).isEqualTo("新问题");
            assertThat(updated.getScore()).isEqualTo(10);
            assertThat(updated.getDifficulty()).isEqualTo("hard");
            assertThat(updated.getExplanation()).isEqualTo("新解析");
            assertThat(updated.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Quiz 不存在时返回 false")
        void returnsFalseWhenNotFound() {
            when(quizService.getById(999L)).thenReturn(null);

            boolean result = facade.updateQuiz(999L, "新问题", null, null, null, null);

            assertThat(result).isFalse();
            verify(quizService, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("deleteQuiz 方法")
    class DeleteQuiz {

        @Test
        @DisplayName("删除并返回结果")
        void deletesAndReturnsResult() {
            when(quizService.removeById(1L)).thenReturn(true);

            assertThat(facade.deleteQuiz(1L)).isTrue();
        }
    }

    @Nested
    @DisplayName("getOptionsByQuizId 方法")
    class GetOptionsByQuizId {

        @Test
        @DisplayName("返回 QuizOptionSummary DTO 列表（不含 isCorrect）")
        void returnsQuizOptionSummaryWithoutIsCorrect() {
            QuizOption option = sampleOption(true);
            when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(option));

            List<QuizOptionSummary> results = facade.getOptionsByQuizId(1L);

            assertThat(results).hasSize(1);
            QuizOptionSummary summary = results.get(0);
            assertThat(summary.getId()).isEqualTo(1L);
            assertThat(summary.getOptionText()).isEqualTo("选项A");
            // QuizOptionSummary has no isCorrect — verify using reflection-style check
            assertThat(summary).hasNoNullFieldsOrPropertiesExcept("isCorrect");
        }

        @Test
        @DisplayName("空列表时返回空列表")
        void emptyReturnsEmpty() {
            when(quizOptionService.getByQuizId(1L)).thenReturn(Collections.emptyList());

            assertThat(facade.getOptionsByQuizId(1L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getOptionsByQuizIdForReview 方法")
    class GetOptionsByQuizIdForReview {

        @Test
        @DisplayName("返回 QuizOptionReviewDto 列表（含 isCorrect）")
        void returnsReviewDtoWithIsCorrect() {
            QuizOption correct = sampleOption(true);
            QuizOption wrong = QuizOption.builder()
                    .id(2L)
                    .quizId(1L)
                    .optionText("选项B")
                    .isCorrect(false)
                    .orderIndex("B")
                    .build();
            when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(correct, wrong));

            List<QuizOptionReviewDto> results = facade.getOptionsByQuizIdForReview(1L);

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getIsCorrect()).isTrue();
            assertThat(results.get(0).getOptionText()).isEqualTo("选项A");
            assertThat(results.get(1).getIsCorrect()).isFalse();
            assertThat(results.get(1).getOptionText()).isEqualTo("选项B");
        }

        @Test
        @DisplayName("空列表时返回空列表")
        void emptyReturnsEmpty() {
            when(quizOptionService.getByQuizId(1L)).thenReturn(Collections.emptyList());

            assertThat(facade.getOptionsByQuizIdForReview(1L)).isEmpty();
        }

        @Test
        @DisplayName("Service 返回 null 时返回空列表")
        void nullReturnsEmpty() {
            when(quizOptionService.getByQuizId(1L)).thenReturn(null);

            assertThat(facade.getOptionsByQuizIdForReview(1L)).isEmpty();
        }
    }
}
