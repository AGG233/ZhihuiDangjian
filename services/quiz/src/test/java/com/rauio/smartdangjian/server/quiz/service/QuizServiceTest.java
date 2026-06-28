package com.rauio.smartdangjian.server.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.request.QuizRequest;
import com.rauio.smartdangjian.server.quiz.pojo.response.QuizResponse;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        assistant.setCurrentNamespace(Quiz.class.getName());
        TableInfoHelper.initTableInfo(assistant, Quiz.class);
    }

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

    // ==================== searchAdminQuizzes ====================

    @Nested
    @DisplayName("searchAdminQuizzes 分页查询测验")
    class SearchAdminQuizzes {

        @Test
        @DisplayName("全部参数为空时返回全量分页结果")
        void returnsPagedResultWithNoFilters() {
            Quiz q1 = Quiz.builder()
                    .id(1L)
                    .question("Q1")
                    .chapterId(1L)
                    .difficulty("easy")
                    .build();
            Quiz q2 = Quiz.builder()
                    .id(2L)
                    .question("Q2")
                    .chapterId(1L)
                    .difficulty("hard")
                    .build();
            Page<Quiz> quizPage = new Page<>(1, 10, 2);
            quizPage.setRecords(List.of(q1, q2));
            doReturn(quizPage).when(quizService).page(any(Page.class), any(LambdaQueryWrapper.class));

            Page<QuizResponse> result = quizService.searchAdminQuizzes(null, null, null, null, 1, 10);

            assertThat(result.getRecords()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(2);
            assertThat(result.getRecords().get(0).getQuestion()).isEqualTo("Q1");
        }

        @Test
        @DisplayName("按 chapterId 筛选")
        void filtersByChapterId() {
            Quiz q1 = Quiz.builder().id(1L).question("Q1").chapterId(1L).build();
            Page<Quiz> quizPage = new Page<>(1, 10, 1);
            quizPage.setRecords(List.of(q1));
            doReturn(quizPage).when(quizService).page(any(Page.class), any(LambdaQueryWrapper.class));

            Page<QuizResponse> result = quizService.searchAdminQuizzes(1L, null, null, null, 1, 10);

            assertThat(result.getRecords()).hasSize(1);
        }

        @Test
        @DisplayName("按 difficulty 筛选")
        void filtersByDifficulty() {
            Quiz q1 = Quiz.builder().id(1L).question("Q1").difficulty("hard").build();
            Page<Quiz> quizPage = new Page<>(1, 10, 1);
            quizPage.setRecords(List.of(q1));
            doReturn(quizPage).when(quizService).page(any(Page.class), any(LambdaQueryWrapper.class));

            Page<QuizResponse> result = quizService.searchAdminQuizzes(null, "hard", null, null, 1, 10);

            assertThat(result.getRecords()).hasSize(1);
        }

        @Test
        @DisplayName("按 isActive 筛选")
        void filtersByIsActive() {
            Quiz q1 = Quiz.builder().id(1L).question("Q1").isActive(true).build();
            Page<Quiz> quizPage = new Page<>(1, 10, 1);
            quizPage.setRecords(List.of(q1));
            doReturn(quizPage).when(quizService).page(any(Page.class), any(LambdaQueryWrapper.class));

            Page<QuizResponse> result = quizService.searchAdminQuizzes(null, null, true, null, 1, 10);

            assertThat(result.getRecords()).hasSize(1);
        }

        @Test
        @DisplayName("按 keyword 模糊搜索")
        void filtersByKeyword() {
            Quiz q1 = Quiz.builder().id(1L).question("关键字匹配").build();
            Page<Quiz> quizPage = new Page<>(1, 10, 1);
            quizPage.setRecords(List.of(q1));
            doReturn(quizPage).when(quizService).page(any(Page.class), any(LambdaQueryWrapper.class));

            Page<QuizResponse> result = quizService.searchAdminQuizzes(null, null, null, "关键字", 1, 10);

            assertThat(result.getRecords()).hasSize(1);
        }

        @Test
        @DisplayName("结果为空时返回空分页")
        void returnsEmptyPageWhenNoResults() {
            Page<Quiz> quizPage = new Page<>(1, 10, 0);
            quizPage.setRecords(Collections.emptyList());
            doReturn(quizPage).when(quizService).page(any(Page.class), any(LambdaQueryWrapper.class));

            Page<QuizResponse> result = quizService.searchAdminQuizzes(null, null, null, null, 1, 10);

            assertThat(result.getRecords()).isEmpty();
            assertThat(result.getTotal()).isZero();
        }
    }

    // ==================== getDifficultyMapByIds ====================

    @Nested
    @DisplayName("getDifficultyMapByIds 批量获取难度映射")
    class GetDifficultyMapByIds {

        @Test
        @DisplayName("传入 null 时返回空 map")
        void returnsEmptyMapWhenIdsIsNull() {
            Map<Long, String> result = quizService.getDifficultyMapByIds(null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("传入空集合时返回空 map")
        void returnsEmptyMapWhenIdsIsEmpty() {
            Map<Long, String> result = quizService.getDifficultyMapByIds(Collections.emptyList());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("返回带难度的 ID 映射")
        void returnsDifficultyMap() {
            Quiz q1 = Quiz.builder().id(1L).difficulty("easy").build();
            Quiz q2 = Quiz.builder().id(2L).difficulty("hard").build();
            doReturn(List.of(q1, q2)).when(quizService).list(any(LambdaQueryWrapper.class));

            Map<Long, String> result = quizService.getDifficultyMapByIds(List.of(1L, 2L));

            assertThat(result).containsOnly(entry(1L, "easy"), entry(2L, "hard"));
        }

        @Test
        @DisplayName("过滤掉 id 或 difficulty 为 null 的条目")
        void filtersNullIdOrDifficulty() {
            Quiz q1 = Quiz.builder().id(1L).difficulty("easy").build();
            Quiz q2 = Quiz.builder().id(2L).difficulty(null).build();
            Quiz q3 = Quiz.builder().id(null).difficulty("hard").build();
            doReturn(List.of(q1, q2, q3)).when(quizService).list(any(LambdaQueryWrapper.class));

            Map<Long, String> result = quizService.getDifficultyMapByIds(List.of(1L, 2L, 3L));

            assertThat(result).containsOnly(entry(1L, "easy"));
        }
    }

    // ==================== create (overloaded) ====================

    @Test
    @DisplayName("create(QuizRequest) 从请求对象创建测验成功")
    void createFromRequestReturnsTrueOnSuccess() {
        QuizRequest request = new QuizRequest(1L, "题目", "single_choice", 5, "easy", "解析", true);
        doReturn(true).when(quizService).save(any(Quiz.class));

        Boolean result = quizService.create(request);

        assertThat(result).isTrue();
        ArgumentCaptor<Quiz> captor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizService).save(captor.capture());
        assertThat(captor.getValue().getChapterId()).isEqualTo(1L);
        assertThat(captor.getValue().getQuestion()).isEqualTo("题目");
        assertThat(captor.getValue().getDifficulty()).isEqualTo("easy");
    }

    // ==================== update (overloaded) ====================

    @Test
    @DisplayName("update(Long, QuizRequest) 从请求对象更新测验成功")
    void updateFromRequestReturnsTrueOnSuccess() {
        QuizRequest request = new QuizRequest(1L, "更新题目", "single_choice", 5, "hard", "新解析", false);
        doReturn(true).when(quizService).updateById(any(Quiz.class));

        Boolean result = quizService.update(10L, request);

        assertThat(result).isTrue();
        ArgumentCaptor<Quiz> captor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizService).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getQuestion()).isEqualTo("更新题目");
        assertThat(captor.getValue().getDifficulty()).isEqualTo("hard");
        assertThat(captor.getValue().getIsActive()).isFalse();
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
