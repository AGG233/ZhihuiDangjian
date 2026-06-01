package com.rauio.smartdangjian.server.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @BeforeEach
    void resetSpy() {
        reset(quizService);
    }

    // ==================== get ====================

    @Test
    @DisplayName("get 根据 quizId 返回测验实体")
    void getReturnsQuizWhenExists() {
        Quiz quiz = Quiz.builder().id(1L).question("测试题目").build();
        doReturn(quiz).when(quizService).getById(1L);

        Quiz result = quizService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getQuestion()).isEqualTo("测试题目");
    }

    @Test
    @DisplayName("get 测验不存在时返回 null")
    void getReturnsNullWhenQuizNotFound() {
        doReturn(null).when(quizService).getById(999L);

        Quiz result = quizService.get(999L);

        assertThat(result).isNull();
    }

    // ==================== getByChapterId ====================

    @Test
    @DisplayName("getByChapterId 根据章节 ID 返回测验列表")
    void getByChapterIdReturnsQuizList() {
        Quiz quiz1 = Quiz.builder().id(1L).chapterId(1L).question("Q1").build();
        Quiz quiz2 = Quiz.builder().id(1L).chapterId(1L).question("Q2").build();
        doReturn(List.of(quiz1, quiz2)).when(quizService).list(any(Wrapper.class));

        List<Quiz> result = quizService.getByChapterId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Quiz::getChapterId).containsOnly(1L);
    }

    @Test
    @DisplayName("getByChapterId 章节下无测验时返回空列表")
    void getByChapterIdReturnsEmptyListWhenNoQuizzes() {
        doReturn(List.of()).when(quizService).list(any(Wrapper.class));

        List<Quiz> result = quizService.getByChapterId(1L);

        assertThat(result).isEmpty();
    }

    // ==================== update ====================

    @Test
    @DisplayName("update 更新测验信息成功返回 true")
    void updateReturnsTrueOnSuccess() {
        Quiz quiz = Quiz.builder().id(1L).question("更新后的题目").build();
        doReturn(true).when(quizService).updateById(quiz);

        Boolean result = quizService.update(quiz);

        assertThat(result).isTrue();
        verify(quizService).updateById(quiz);
    }

    @Test
    @DisplayName("update 更新失败时返回 false")
    void updateReturnsFalseOnFailure() {
        Quiz quiz = Quiz.builder().id(1L).question("更新后的题目").build();
        doReturn(false).when(quizService).updateById(quiz);

        Boolean result = quizService.update(quiz);

        assertThat(result).isFalse();
    }

    // ==================== create ====================

    @Test
    @DisplayName("create 创建测验成功返回 true")
    void createReturnsTrueOnSuccess() {
        Quiz quiz = Quiz.builder().chapterId(1L).question("新题目").build();
        doReturn(true).when(quizService).save(quiz);

        Boolean result = quizService.create(quiz);

        assertThat(result).isTrue();
        ArgumentCaptor<Quiz> quizCaptor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizService).save(quizCaptor.capture());
        assertThat(quizCaptor.getValue().getChapterId()).isEqualTo(1L);
        assertThat(quizCaptor.getValue().getQuestion()).isEqualTo("新题目");
    }

    @Test
    @DisplayName("create 创建失败时返回 false")
    void createReturnsFalseOnFailure() {
        Quiz quiz = Quiz.builder().chapterId(1L).question("新题目").build();
        doReturn(false).when(quizService).save(quiz);

        Boolean result = quizService.create(quiz);

        assertThat(result).isFalse();
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 删除测验成功返回 true")
    void deleteReturnsTrueOnSuccess() {
        doReturn(true).when(quizService).removeById(1L);

        Boolean result = quizService.delete(1L);

        assertThat(result).isTrue();
        verify(quizService).removeById(1L);
    }

    @Test
    @DisplayName("delete 删除失败时返回 false")
    void deleteReturnsFalseOnFailure() {
        doReturn(false).when(quizService).removeById(1L);

        Boolean result = quizService.delete(1L);

        assertThat(result).isFalse();
    }
}
