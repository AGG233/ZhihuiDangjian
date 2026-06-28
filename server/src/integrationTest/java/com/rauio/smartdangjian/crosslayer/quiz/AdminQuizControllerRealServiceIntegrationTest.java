package com.rauio.smartdangjian.crosslayer.quiz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.quiz.controller.admin.AdminQuizController;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.response.QuizResponse;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

@SpringBootTest(classes = AdminQuizControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("管理员试题控制层集成测试")
class AdminQuizControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private QuizOptionService quizOptionService;

    @BeforeEach
    void setUp() {
        reset(quizService, quizOptionService);
        setSchoolContext(1L, "uni-1");
    }

    @Test
    @DisplayName("GET /api/admin/quiz/quizzes/{id} 成功返回试题详情")
    void getQuiz() throws Exception {
        Quiz quiz = Quiz.builder().id(100L).question("测试题目").build();
        when(quizService.get(100L)).thenReturn(quiz);

        mockMvc.perform(get("/api/admin/quiz/quizzes/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.question").value("测试题目"));

        verify(quizService).get(100L);
    }

    @Test
    @DisplayName("GET /api/admin/quiz/quizzes 成功返回分页试题列表")
    void getQuizPage() throws Exception {
        Page<QuizResponse> page = new Page<>(1, 10);
        page.setRecords(List.of(QuizResponse.builder().id(100L).question("题目1").build()));
        page.setTotal(1);
        when(quizService.searchAdminQuizzes(null, null, null, null, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/admin/quiz/quizzes").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/admin/quiz/quizzes 成功创建试题")
    void createQuiz() throws Exception {
        when(quizService.create(any(com.rauio.smartdangjian.server.quiz.pojo.request.QuizRequest.class)))
                .thenReturn(true);

        mockMvc.perform(
                        post("/api/admin/quiz/quizzes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"chapterId":1,"question":"测试题目","questionType":"single_choice",
                                 "score":5,"difficulty":"medium","isActive":true,"explanation":"解析"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("PUT /api/admin/quiz/quizzes/{id} 成功更新试题")
    void updateQuiz() throws Exception {
        when(quizService.update(any(), any(com.rauio.smartdangjian.server.quiz.pojo.request.QuizRequest.class)))
                .thenReturn(true);

        mockMvc.perform(
                        put("/api/admin/quiz/quizzes/100")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"chapterId":1,"question":"更新题目","questionType":"single_choice",
                                 "score":10,"difficulty":"hard","isActive":true,"explanation":"新解析"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("DELETE /api/admin/quiz/quizzes/{id} 成功删除试题")
    void deleteQuiz() throws Exception {
        when(quizService.delete(100L)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/quiz/quizzes/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(quizService).delete(100L);
    }

    @Test
    @DisplayName("POST /api/admin/quiz/quizzes/{id}/options 成功创建选项")
    void createQuizOption() throws Exception {
        when(quizOptionService.create(
                        any(), any(com.rauio.smartdangjian.server.quiz.pojo.request.QuizOptionRequest.class)))
                .thenReturn(true);

        mockMvc.perform(post("/api/admin/quiz/quizzes/100/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionText\":\"选项A\",\"isCorrect\":true,\"orderIndex\":\"A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("PUT /api/admin/quiz/quizzes/{quizId}/options/{optionId} 成功更新选项")
    void updateQuizOption() throws Exception {
        when(quizOptionService.update(
                        any(), any(com.rauio.smartdangjian.server.quiz.pojo.request.QuizOptionRequest.class)))
                .thenReturn(true);

        mockMvc.perform(put("/api/admin/quiz/quizzes/100/options/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionText\":\"选项B\",\"isCorrect\":false,\"orderIndex\":\"B\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("DELETE /api/admin/quiz/quizzes/{quizId}/options/{optionId} 成功删除选项")
    void deleteQuizOption() throws Exception {
        when(quizOptionService.delete(50L)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/quiz/quizzes/100/options/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(quizOptionService).delete(50L);
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        AdminQuizController adminQuizController(QuizService quizService, QuizOptionService quizOptionService) {
            return new AdminQuizController(quizService, quizOptionService);
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
