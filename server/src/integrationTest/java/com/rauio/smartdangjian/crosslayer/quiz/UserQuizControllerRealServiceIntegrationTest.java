package com.rauio.smartdangjian.crosslayer.quiz;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.quiz.controller.user.UserQuizController;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

@SpringBootTest(classes = UserQuizControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("用户试题控制层集成测试")
class UserQuizControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private QuizOptionService quizOptionService;

    @BeforeEach
    void setUp() {
        reset(quizService, quizOptionService);
        setStudentContext(1L, "uni-1");
    }

    @Test
    @DisplayName("GET /api/quiz/quizzes/{id} 成功返回试题详情")
    void getQuiz() throws Exception {
        Quiz quiz = Quiz.builder().id(100L).question("测试题目").chapterId(1L).build();
        when(quizService.get(100L)).thenReturn(quiz);

        mockMvc.perform(get("/api/quiz/quizzes/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.question").value("测试题目"));

        verify(quizService).get(100L);
    }

    @Test
    @DisplayName("GET /api/quiz/quizzes/by-chapter/{chapterId} 成功返回章节试题列表")
    void getQuizByChapter() throws Exception {
        Quiz quiz1 = Quiz.builder().id(100L).question("题目1").chapterId(1L).build();
        Quiz quiz2 = Quiz.builder().id(101L).question("题目2").chapterId(1L).build();
        when(quizService.getByChapterId(1L)).thenReturn(List.of(quiz1, quiz2));

        mockMvc.perform(get("/api/quiz/quizzes/by-chapter/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].question").value("题目1"))
                .andExpect(jsonPath("$.data[1].question").value("题目2"));

        verify(quizService).getByChapterId(1L);
    }

    @Test
    @DisplayName("GET /api/quiz/quizzes/{id}/options 成功返回选项列表")
    void getQuizOptions() throws Exception {
        QuizOption option = QuizOption.builder()
                .id(50L)
                .quizId(100L)
                .orderIndex("A")
                .optionText("选项A")
                .build();
        when(quizOptionService.getByQuizId(100L)).thenReturn(List.of(option));

        mockMvc.perform(get("/api/quiz/quizzes/100/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].orderIndex").value("A"))
                .andExpect(jsonPath("$.data[0].optionText").value("选项A"));

        verify(quizOptionService).getByQuizId(100L);
    }

    @Test
    @DisplayName("GET /api/quiz/quizzes/{id}/options/{optionId} 成功返回选项详情")
    void getQuizOptionById() throws Exception {
        QuizOption option = QuizOption.builder()
                .id(50L)
                .quizId(100L)
                .orderIndex("A")
                .optionText("选项A")
                .isCorrect(true)
                .build();
        when(quizOptionService.get(50L)).thenReturn(option);

        mockMvc.perform(get("/api/quiz/quizzes/100/options/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.orderIndex").value("A"))
                .andExpect(jsonPath("$.data.optionText").value("选项A"));

        verify(quizOptionService).get(50L);
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserQuizController userQuizController(QuizService quizService, QuizOptionService quizOptionService) {
            return new UserQuizController(quizService, quizOptionService);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }
}
