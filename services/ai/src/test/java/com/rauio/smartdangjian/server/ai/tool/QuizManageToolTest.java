package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.api.QuizDataFacade;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizSummary;

@ExtendWith(MockitoExtension.class)
class QuizManageToolTest {

    @Mock
    private QuizDataFacade quizDataFacade;

    private QuizManageTool quizManageTool;

    @BeforeEach
    void setUp() {
        quizManageTool = new QuizManageTool(quizDataFacade);
    }

    @Test
    @DisplayName("getQuiz 返回存在的测验")
    void getQuizReturnsExistingQuiz() {
        QuizSummary quiz =
                QuizSummary.builder().id(1L).question("What is Java?").build();
        when(quizDataFacade.getQuiz(1L)).thenReturn(quiz);

        QuizSummary result = quizManageTool.getQuiz("1");

        assertThat(result).isEqualTo(quiz);
    }

    @Test
    @DisplayName("getQuiz 测验不存在时抛出 BusinessException")
    void getQuizThrowsWhenNotFound() {
        when(quizDataFacade.getQuiz(9999L)).thenReturn(null);

        assertThatThrownBy(() -> quizManageTool.getQuiz("9999"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验不存在");
    }

    @Test
    @DisplayName("createQuiz 成功创建测验及选项")
    void createQuizSavesQuizAndOptions() {
        when(quizDataFacade.createQuiz(any(), anyString(), anyString(), anyInt(), anyString(), anyString(), anyList()))
                .thenReturn(1L);

        List<Map<String, Object>> options = List.of(
                Map.of("optionText", "A", "isCorrect", true, "orderIndex", "A"),
                Map.of("optionText", "B", "isCorrect", false, "orderIndex", "B"));

        Boolean result = quizManageTool.createQuiz("1", "Q1", "single_choice", 5, "easy", "explanation", options);

        assertThat(result).isTrue();
        verify(quizDataFacade).createQuiz(1L, "Q1", "single_choice", 5, "easy", "explanation", options);
    }

    @Test
    @DisplayName("createQuiz 选项为 null 时只创建测验")
    void createQuizWithNullOptionsSkipsOptions() {
        when(quizDataFacade.createQuiz(any(), anyString(), anyString(), anyInt(), anyString(), any(), any()))
                .thenReturn(1L);

        Boolean result = quizManageTool.createQuiz("1", "Q2", "true_false", 2, "easy", null, null);

        assertThat(result).isTrue();
        verify(quizDataFacade).createQuiz(1L, "Q2", "true_false", 2, "easy", null, null);
    }

    @Test
    @DisplayName("createQuiz 测验创建失败时抛出 BusinessException")
    void createQuizThrowsWhenSaveFails() {
        when(quizDataFacade.createQuiz(any(), anyString(), anyString(), anyInt(), anyString(), any(), anyList()))
                .thenReturn(null);

        assertThatThrownBy(() ->
                        quizManageTool.createQuiz("1", "Q3", "single_choice", 5, "easy", null, Collections.emptyList()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("测验创建失败");
    }

    @Test
    @DisplayName("createQuiz with null optionText")
    void createQuizWithNullOptionText() {
        when(quizDataFacade.createQuiz(eq(1L), eq("Q"), eq("single_choice"), eq(5), eq("easy"), eq(null), anyList()))
                .thenReturn(1L);

        List<Map<String, Object>> options = List.of(Map.of("orderIndex", "A"));

        Boolean result = quizManageTool.createQuiz("1", "Q", "single_choice", 5, "easy", null, options);

        assertThat(result).isTrue();
        verify(quizDataFacade).createQuiz(1L, "Q", "single_choice", 5, "easy", null, options);
    }

    @Test
    @DisplayName("createQuiz with null orderIndex")
    void createQuizWithNullOrderIndex() {
        when(quizDataFacade.createQuiz(eq(1L), eq("Q"), eq("single_choice"), eq(5), eq("easy"), eq(null), anyList()))
                .thenReturn(1L);

        List<Map<String, Object>> options = List.of(Map.of("optionText", "A", "isCorrect", true));

        Boolean result = quizManageTool.createQuiz("1", "Q", "single_choice", 5, "easy", null, options);

        assertThat(result).isTrue();
        verify(quizDataFacade).createQuiz(1L, "Q", "single_choice", 5, "easy", null, options);
    }

    @Test
    @DisplayName("deleteQuiz 成功删除测验")
    void deleteQuizRemovesQuiz() {
        when(quizDataFacade.deleteQuiz(1L)).thenReturn(true);

        Boolean result = quizManageTool.deleteQuiz("1");

        assertThat(result).isTrue();
        verify(quizDataFacade).deleteQuiz(1L);
    }

    @Test
    @DisplayName("deleteQuiz 删除失败时返回 false")
    void deleteQuizReturnsFalseWhenDeleteFails() {
        when(quizDataFacade.deleteQuiz(1L)).thenReturn(false);

        Boolean result = quizManageTool.deleteQuiz("1");

        assertThat(result).isFalse();
        verify(quizDataFacade).deleteQuiz(1L);
    }

    @Nested
    @DisplayName("updateQuiz 方法")
    class UpdateQuizTest {

        @Test
        @DisplayName("全字段更新成功")
        void fullUpdate() {
            when(quizDataFacade.updateQuiz(eq(1L), eq("新题目"), eq(10), eq("hard"), eq("新解析"), eq(false)))
                    .thenReturn(true);

            Boolean result = quizManageTool.updateQuiz("1", "新题目", 10, "hard", "新解析", false);

            assertThat(result).isTrue();
            verify(quizDataFacade).updateQuiz(1L, "新题目", 10, "hard", "新解析", false);
        }

        @Test
        @DisplayName("部分字段更新时只传递非空字段")
        void partialUpdate() {
            when(quizDataFacade.updateQuiz(eq(1L), eq("新题"), eq(null), eq("hard"), eq(null), eq(null)))
                    .thenReturn(true);

            Boolean result = quizManageTool.updateQuiz("1", "新题", null, "hard", null, null);

            assertThat(result).isTrue();
            verify(quizDataFacade).updateQuiz(1L, "新题", null, "hard", null, null);
        }

        @Test
        @DisplayName("update 返回 false 时返回 false")
        void returnsFalseWhenUpdateFails() {
            when(quizDataFacade.updateQuiz(eq(1L), eq("新题"), eq(null), eq(null), eq(null), eq(null)))
                    .thenReturn(false);

            Boolean result = quizManageTool.updateQuiz("1", "新题", null, null, null, null);

            assertThat(result).isFalse();
            verify(quizDataFacade).updateQuiz(1L, "新题", null, null, null, null);
        }
    }

    @Nested
    @DisplayName("searchQuizzesByChapter 方法")
    class SearchQuizzesByChapterTest {

        @Test
        @DisplayName("返回该章节下的所有测验")
        void returnsQuizzesForChapter() {
            QuizSummary quiz1 = QuizSummary.builder().id(1L).question("题1").build();
            QuizSummary quiz2 = QuizSummary.builder().id(2L).question("题2").build();
            when(quizDataFacade.getQuizzesByChapter(1L)).thenReturn(List.of(quiz1, quiz2));

            List<QuizSummary> result = quizManageTool.searchQuizzesByChapter("1");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getQuestion()).isEqualTo("题1");
            assertThat(result.get(1).getQuestion()).isEqualTo("题2");
        }

        @ParameterizedTest(name = "chapterId=''{0}''")
        @NullSource
        @EmptySource
        @DisplayName("null/empty chapterId 时抛出异常")
        void throwsWhenChapterIdIsNullOrEmpty(String chapterId) {
            assertThatThrownBy(() -> quizManageTool.searchQuizzesByChapter(chapterId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("无测验时返回空列表")
        void returnsEmptyListWhenNoQuizzes() {
            when(quizDataFacade.getQuizzesByChapter(1L)).thenReturn(Collections.emptyList());

            List<QuizSummary> result = quizManageTool.searchQuizzesByChapter("1");

            assertThat(result).isEmpty();
        }
    }
}
