package com.rauio.smartdangjian.server.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizMapper mapper;

    @Spy
    @InjectMocks
    private QuizService quizService;

    // ==================== get ====================

    @Test
    @DisplayName("get 根据 quizId 返回测验实体")
    void getReturnsQuizWhenExists() {
        Quiz quiz = Quiz.builder().id("quiz-1").question("测试题目").build();
        doReturn(quiz).when(quizService).getById("quiz-1");

        Quiz result = quizService.get("quiz-1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("quiz-1");
        assertThat(result.getQuestion()).isEqualTo("测试题目");
    }

    @Test
    @DisplayName("get 测验不存在时返回 null")
    void getReturnsNullWhenQuizNotFound() {
        doReturn(null).when(quizService).getById("non-existent-id");

        Quiz result = quizService.get("non-existent-id");

        assertThat(result).isNull();
    }

    // ==================== getByChapterId ====================

    @Test
    @DisplayName("getByChapterId 根据章节 ID 返回测验列表")
    void getByChapterIdReturnsQuizList() {
        Quiz quiz1 = Quiz.builder()
                .id("quiz-1")
                .chapterId("chapter-1")
                .question("Q1")
                .build();
        Quiz quiz2 = Quiz.builder()
                .id("quiz-2")
                .chapterId("chapter-1")
                .question("Q2")
                .build();
        doReturn(List.of(quiz1, quiz2)).when(quizService).list(any(Wrapper.class));

        List<Quiz> result = quizService.getByChapterId("chapter-1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Quiz::getChapterId).containsOnly("chapter-1");
    }

    @Test
    @DisplayName("getByChapterId 章节下无测验时返回空列表")
    void getByChapterIdReturnsEmptyListWhenNoQuizzes() {
        doReturn(List.of()).when(quizService).list(any(Wrapper.class));

        List<Quiz> result = quizService.getByChapterId("empty-chapter");

        assertThat(result).isEmpty();
    }

    // ==================== update ====================

    @Test
    @DisplayName("update 更新测验信息成功返回 true")
    void updateReturnsTrueOnSuccess() {
        Quiz quiz = Quiz.builder().id("quiz-1").question("更新后的题目").build();
        doReturn(true).when(quizService).updateById(quiz);

        Boolean result = quizService.update(quiz);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("update 更新失败时返回 false")
    void updateReturnsFalseOnFailure() {
        Quiz quiz = Quiz.builder().id("quiz-1").question("更新后的题目").build();
        doReturn(false).when(quizService).updateById(quiz);

        Boolean result = quizService.update(quiz);

        assertThat(result).isFalse();
    }

    // ==================== create ====================

    @Test
    @DisplayName("create 创建测验成功返回 true")
    void createReturnsTrueOnSuccess() {
        Quiz quiz = Quiz.builder().chapterId("chapter-1").question("新题目").build();
        doReturn(true).when(quizService).save(quiz);

        Boolean result = quizService.create(quiz);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("create 创建失败时返回 false")
    void createReturnsFalseOnFailure() {
        Quiz quiz = Quiz.builder().chapterId("chapter-1").question("新题目").build();
        doReturn(false).when(quizService).save(quiz);

        Boolean result = quizService.create(quiz);

        assertThat(result).isFalse();
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 删除测验成功返回 true")
    void deleteReturnsTrueOnSuccess() {
        doReturn(true).when(quizService).removeById("quiz-1");

        Boolean result = quizService.delete("quiz-1");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete 删除失败时返回 false")
    void deleteReturnsFalseOnFailure() {
        doReturn(false).when(quizService).removeById("quiz-1");

        Boolean result = quizService.delete("quiz-1");

        assertThat(result).isFalse();
    }
}
