package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

@ExtendWith(MockitoExtension.class)
class QuizManageToolTest {

    @Mock
    private QuizService quizService;

    @Mock
    private QuizOptionService quizOptionService;

    @InjectMocks
    private QuizManageTool quizManageTool;

    @Test
    @DisplayName("getQuiz 返回存在的测验")
    void getQuizReturnsExistingQuiz() {
        Quiz quiz = Quiz.builder().id("quiz-1").question("What is Java?").build();
        when(quizService.get("quiz-1")).thenReturn(quiz);

        Quiz result = quizManageTool.getQuiz("quiz-1");

        assertThat(result).isEqualTo(quiz);
    }

    @Test
    @DisplayName("getQuiz 测验不存在时抛出 BusinessException")
    void getQuizThrowsWhenNotFound() {
        when(quizService.get("missing-id")).thenReturn(null);

        assertThatThrownBy(() -> quizManageTool.getQuiz("missing-id"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验不存在");
    }

    @Test
    @DisplayName("createQuiz 成功创建测验及选项")
    void createQuizSavesQuizAndOptions() {
        Quiz savedQuiz = Quiz.builder().id("new-quiz-id").build();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId("new-quiz-id");
            return true;
        });
        when(quizOptionService.create(any(), any(QuizOption.class))).thenReturn(true);

        List<Map<String, Object>> options = List.of(
                Map.of("optionText", "A", "isCorrect", true, "orderIndex", "A"),
                Map.of("optionText", "B", "isCorrect", false, "orderIndex", "B"));

        Boolean result =
                quizManageTool.createQuiz("chapter-1", "Q1", "single_choice", 5, "easy", "explanation", options);

        assertThat(result).isTrue();
        verify(quizService, times(1))
                .create(argThat(q -> q.getChapterId().equals("chapter-1")
                        && q.getQuestion().equals("Q1")
                        && q.getQuestionType().equals("single_choice")
                        && q.getScore().equals(5)
                        && q.getDifficulty().equals("easy")
                        && q.getExplanation().equals("explanation")
                        && Boolean.TRUE.equals(q.getIsActive())));
        verify(quizOptionService, times(2)).create(any(), any(QuizOption.class));
    }

    @Test
    @DisplayName("createQuiz 选项为 null 时只创建测验")
    void createQuizWithNullOptionsSkipsOptions() {
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId("quiz-id");
            return true;
        });

        Boolean result = quizManageTool.createQuiz("chapter-1", "Q2", "true_false", 2, "easy", null, null);

        assertThat(result).isTrue();
        verify(quizOptionService, never()).create(any(), any(QuizOption.class));
    }

    @Test
    @DisplayName("createQuiz 测验保存失败时抛出 BusinessException")
    void createQuizThrowsWhenSaveFails() {
        when(quizService.create(any(Quiz.class))).thenReturn(false);

        assertThatThrownBy(() -> quizManageTool.createQuiz(
                        "chapter-1", "Q3", "single_choice", 5, "easy", null, Collections.emptyList()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验创建失败");
    }

    @Test
    @DisplayName("createQuiz 选项保存失败时抛出 BusinessException")
    void createQuizThrowsWhenOptionSaveFails() {
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId("quiz-id");
            return true;
        });
        when(quizOptionService.create(any(), any(QuizOption.class))).thenReturn(false);

        List<Map<String, Object>> options = List.of(Map.of("optionText", "A", "isCorrect", true, "orderIndex", "A"));

        assertThatThrownBy(
                        () -> quizManageTool.createQuiz("chapter-1", "Q4", "single_choice", 5, "easy", null, options))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("选项创建失败");
    }

    @Test
    @DisplayName("deleteQuiz 级联删除测验及其选项")
    void deleteQuizRemovesQuizAndOptions() {
        Quiz quiz = Quiz.builder().id("quiz-1").build();
        QuizOption opt1 = QuizOption.builder().id("opt-1").quizId("quiz-1").build();
        QuizOption opt2 = QuizOption.builder().id("opt-2").quizId("quiz-1").build();

        when(quizService.get("quiz-1")).thenReturn(quiz);
        when(quizOptionService.getByQuizId("quiz-1")).thenReturn(List.of(opt1, opt2));
        when(quizService.delete("quiz-1")).thenReturn(true);

        Boolean result = quizManageTool.deleteQuiz("quiz-1");

        assertThat(result).isTrue();
        verify(quizOptionService, times(1)).delete("opt-1");
        verify(quizOptionService, times(1)).delete("opt-2");
        verify(quizService, times(1)).delete("quiz-1");
    }

    @Test
    @DisplayName("deleteQuiz 无选项时直接删除测验")
    void deleteQuizWithNoOptionsDeletesQuizOnly() {
        Quiz quiz = Quiz.builder().id("quiz-1").build();
        when(quizService.get("quiz-1")).thenReturn(quiz);
        when(quizOptionService.getByQuizId("quiz-1")).thenReturn(null);
        when(quizService.delete("quiz-1")).thenReturn(true);

        Boolean result = quizManageTool.deleteQuiz("quiz-1");

        assertThat(result).isTrue();
        verify(quizOptionService, never()).delete(any());
        verify(quizService, times(1)).delete("quiz-1");
    }

    @Test
    @DisplayName("deleteQuiz 测验不存在时抛出 BusinessException")
    void deleteQuizThrowsWhenNotFound() {
        when(quizService.get("missing-id")).thenReturn(null);

        assertThatThrownBy(() -> quizManageTool.deleteQuiz("missing-id"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验不存在");
    }
}
