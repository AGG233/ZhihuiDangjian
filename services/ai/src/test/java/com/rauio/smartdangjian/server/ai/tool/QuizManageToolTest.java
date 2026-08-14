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
import org.junit.jupiter.api.Nested;
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
        Quiz quiz = Quiz.builder().id(1L).question("What is Java?").build();
        when(quizService.get(1L)).thenReturn(quiz);

        Quiz result = quizManageTool.getQuiz("1");

        assertThat(result).isEqualTo(quiz);
    }

    @Test
    @DisplayName("getQuiz 测验不存在时抛出 BusinessException")
    void getQuizThrowsWhenNotFound() {
        when(quizService.get(9999L)).thenReturn(null);

        assertThatThrownBy(() -> quizManageTool.getQuiz("9999"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验不存在");
    }

    @Test
    @DisplayName("createQuiz 成功创建测验及选项")
    void createQuizSavesQuizAndOptions() {
        Quiz savedQuiz = Quiz.builder().id(1L).build();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(any(), any(QuizOption.class))).thenReturn(true);

        List<Map<String, Object>> options = List.of(
                Map.of("optionText", "A", "isCorrect", true, "orderIndex", "A"),
                Map.of("optionText", "B", "isCorrect", false, "orderIndex", "B"));

        Boolean result = quizManageTool.createQuiz("1", "Q1", "single_choice", 5, "easy", "explanation", options);

        assertThat(result).isTrue();
        verify(quizService, times(1))
                .create(argThat(q -> q.getChapterId().equals(1L)
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
            q.setId(1L);
            return true;
        });

        Boolean result = quizManageTool.createQuiz("1", "Q2", "true_false", 2, "easy", null, null);

        assertThat(result).isTrue();
        verify(quizOptionService, never()).create(any(), any(QuizOption.class));
    }

    @Test
    @DisplayName("createQuiz isCorrect 为非布尔类型时转为 null")
    void createQuizWithNonBooleanIsCorrect() {
        Quiz savedQuiz = Quiz.builder().id(1L).build();
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(any(), any(QuizOption.class))).thenReturn(true);

        List<Map<String, Object>> options = List.of(Map.of("optionText", "A", "isCorrect", "yes", "orderIndex", "A"));

        Boolean result = quizManageTool.createQuiz("1", "Q", "single_choice", 5, "easy", null, options);

        assertThat(result).isTrue();
        verify(quizOptionService).create(any(), argThat(opt -> opt.getIsCorrect() == null));
    }

    @Test
    @DisplayName("createQuiz 测验保存失败时抛出 BusinessException")
    void createQuizThrowsWhenSaveFails() {
        when(quizService.create(any(Quiz.class))).thenReturn(false);

        assertThatThrownBy(() ->
                        quizManageTool.createQuiz("1", "Q3", "single_choice", 5, "easy", null, Collections.emptyList()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验创建失败");
    }

    @Test
    @DisplayName("createQuiz 选项保存失败时抛出 BusinessException")
    void createQuizThrowsWhenOptionSaveFails() {
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(any(), any(QuizOption.class))).thenReturn(false);

        List<Map<String, Object>> options = List.of(Map.of("optionText", "A", "isCorrect", true, "orderIndex", "A"));

        assertThatThrownBy(() -> quizManageTool.createQuiz("1", "Q4", "single_choice", 5, "easy", null, options))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("选项创建失败");
    }

    @Test
    @DisplayName("deleteQuiz 级联删除测验及其选项")
    void deleteQuizRemovesQuizAndOptions() {
        Quiz quiz = Quiz.builder().id(1L).build();
        QuizOption opt1 = QuizOption.builder().id(1L).quizId(1L).build();
        QuizOption opt2 = QuizOption.builder().id(2L).quizId(1L).build();

        when(quizService.get(1L)).thenReturn(quiz);
        when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(opt1, opt2));
        when(quizService.delete(1L)).thenReturn(true);

        Boolean result = quizManageTool.deleteQuiz("1");

        assertThat(result).isTrue();
        verify(quizOptionService, times(1)).delete(1L);
        verify(quizOptionService, times(1)).delete(2L);
        verify(quizService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("deleteQuiz 无选项时直接删除测验")
    void deleteQuizWithNoOptionsDeletesQuizOnly() {
        Quiz quiz = Quiz.builder().id(1L).build();
        when(quizService.get(1L)).thenReturn(quiz);
        when(quizOptionService.getByQuizId(1L)).thenReturn(null);
        when(quizService.delete(1L)).thenReturn(true);

        Boolean result = quizManageTool.deleteQuiz("1");

        assertThat(result).isTrue();
        verify(quizOptionService, never()).delete(any());
        verify(quizService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("deleteQuiz 测验不存在时抛出 BusinessException")
    void deleteQuizThrowsWhenNotFound() {
        when(quizService.get(9999L)).thenReturn(null);

        assertThatThrownBy(() -> quizManageTool.deleteQuiz("9999"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验不存在");
    }

    @Test
    @DisplayName("deleteQuiz 删除操作返回 false 时返回 false")
    void deleteQuizReturnsFalseWhenDeleteFails() {
        Quiz quiz = Quiz.builder().id(1L).build();
        when(quizService.get(1L)).thenReturn(quiz);
        when(quizOptionService.getByQuizId(1L)).thenReturn(null);
        when(quizService.delete(1L)).thenReturn(false);

        Boolean result = quizManageTool.deleteQuiz("1");

        assertThat(result).isFalse();
        verify(quizService, times(1)).delete(1L);
    }

    @Nested
    @DisplayName("updateQuiz 方法")
    class UpdateQuizTest {

        @Test
        @DisplayName("全字段更新成功")
        void fullUpdate() {
            Quiz existing = Quiz.builder()
                    .id(1L)
                    .question("旧题目")
                    .score(5)
                    .difficulty("easy")
                    .explanation("旧解析")
                    .isActive(true)
                    .build();
            when(quizService.get(1L)).thenReturn(existing);
            when(quizService.update(existing)).thenReturn(true);

            Boolean result = quizManageTool.updateQuiz("1", "新题目", 10, "hard", "新解析", false);

            assertThat(result).isTrue();
            assertThat(existing.getQuestion()).isEqualTo("新题目");
            assertThat(existing.getScore()).isEqualTo(10);
            assertThat(existing.getDifficulty()).isEqualTo("hard");
            assertThat(existing.getExplanation()).isEqualTo("新解析");
            assertThat(existing.getIsActive()).isFalse();
            verify(quizService, times(1)).update(existing);
        }

        @Test
        @DisplayName("部分字段更新时只修改非空字段")
        void partialUpdate() {
            Quiz existing = Quiz.builder()
                    .id(1L)
                    .question("原题")
                    .score(5)
                    .difficulty("easy")
                    .explanation("原解析")
                    .isActive(true)
                    .build();
            when(quizService.get(1L)).thenReturn(existing);
            when(quizService.update(existing)).thenReturn(true);

            Boolean result = quizManageTool.updateQuiz("1", "新题", null, "hard", null, null);

            assertThat(result).isTrue();
            assertThat(existing.getQuestion()).isEqualTo("新题");
            assertThat(existing.getScore()).isEqualTo(5);
            assertThat(existing.getDifficulty()).isEqualTo("hard");
            assertThat(existing.getExplanation()).isEqualTo("原解析");
            assertThat(existing.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("测验不存在时抛出 BusinessException")
        void throwsWhenNotFound() {
            when(quizService.get(9999L)).thenReturn(null);

            assertThatThrownBy(() -> quizManageTool.updateQuiz("9999", null, null, null, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("测验不存在");
        }

        @Test
        @DisplayName("update only when isActive provided, question stays null")
        void updateWithoutQuestion() {
            Quiz existing = Quiz.builder()
                    .id(1L)
                    .question("原题")
                    .score(5)
                    .difficulty("easy")
                    .explanation("原解析")
                    .isActive(true)
                    .build();
            when(quizService.get(1L)).thenReturn(existing);
            when(quizService.update(existing)).thenReturn(true);

            Boolean result = quizManageTool.updateQuiz("1", null, null, null, null, false);

            assertThat(result).isTrue();
            assertThat(existing.getQuestion()).isEqualTo("原题");
            assertThat(existing.getIsActive()).isFalse();
            verify(quizService, times(1)).update(existing);
        }

        @Test
        @DisplayName("update with all fields as null keeps original values")
        void updateAllNullFields() {
            Quiz existing = Quiz.builder()
                    .id(1L)
                    .question("题")
                    .score(5)
                    .difficulty("easy")
                    .explanation("解析")
                    .isActive(true)
                    .build();
            when(quizService.get(1L)).thenReturn(existing);
            when(quizService.update(existing)).thenReturn(true);

            Boolean result = quizManageTool.updateQuiz("1", null, null, null, null, null);

            assertThat(result).isTrue();
            assertThat(existing.getQuestion()).isEqualTo("题");
            assertThat(existing.getScore()).isEqualTo(5);
            assertThat(existing.getDifficulty()).isEqualTo("easy");
            assertThat(existing.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("update returns false when update fails")
        void returnsFalseWhenUpdateFails() {
            Quiz existing = Quiz.builder().id(1L).question("题").build();
            when(quizService.get(1L)).thenReturn(existing);
            when(quizService.update(existing)).thenReturn(false);

            Boolean result = quizManageTool.updateQuiz("1", "新题", null, null, null, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("searchQuizzesByChapter method")
    class SearchQuizzesByChapterTest {

        @Test
        @DisplayName("returns quizzes for chapter")
        void returnsQuizzesForChapter() {
            Quiz quiz1 = Quiz.builder().id(1L).question("题1").build();
            Quiz quiz2 = Quiz.builder().id(2L).question("题2").build();
            when(quizService.getByChapterId(1L)).thenReturn(List.of(quiz1, quiz2));

            List<Quiz> result = quizManageTool.searchQuizzesByChapter("1");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getQuestion()).isEqualTo("题1");
            assertThat(result.get(1).getQuestion()).isEqualTo("题2");
        }

        @Test
        @DisplayName("returns empty list when no quizzes")
        void returnsEmptyListWhenNoQuizzes() {
            when(quizService.getByChapterId(1L)).thenReturn(Collections.emptyList());

            List<Quiz> result = quizManageTool.searchQuizzesByChapter("1");

            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("createQuiz with null optionText")
    void createQuizWithNullOptionText() {
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(any(), any(QuizOption.class))).thenReturn(true);

        List<Map<String, Object>> options = List.of(Map.of("orderIndex", "A"));

        Boolean result = quizManageTool.createQuiz("1", "Q", "single_choice", 5, "easy", null, options);

        assertThat(result).isTrue();
        verify(quizOptionService).create(any(), argThat(opt -> opt.getOptionText() == null));
    }

    @Test
    @DisplayName("createQuiz with null orderIndex")
    void createQuizWithNullOrderIndex() {
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return true;
        });
        when(quizOptionService.create(any(), any(QuizOption.class))).thenReturn(true);

        List<Map<String, Object>> options = List.of(Map.of("optionText", "A", "isCorrect", true));

        Boolean result = quizManageTool.createQuiz("1", "Q", "single_choice", 5, "easy", null, options);

        assertThat(result).isTrue();
        verify(quizOptionService).create(any(), argThat(opt -> opt.getOrderIndex() == null));
    }

    @Test
    @DisplayName("createQuiz with null quizId")
    void createQuizWithNullQuizIdThrows() {
        when(quizService.create(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(null);
            return true;
        });

        assertThatThrownBy(() -> quizManageTool.createQuiz("1", "Q", "single_choice", 5, "easy", null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验创建失败");
    }
}
