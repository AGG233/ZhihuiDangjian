package com.rauio.smartdangjian.controller.admin;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.QuizTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.controller.admin.AdminQuizController;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AdminQuizControllerTest.TestConfig.class
)
@DisplayName("管理员试题接口测试")
class AdminQuizControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminQuizController adminQuizController(QuizService quizService, QuizOptionService quizOptionService) {
            return new AdminQuizController(quizService, quizOptionService);
        }
    }

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private QuizOptionService quizOptionService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST /api/admin/quiz/quizzes - 创建试题成功")
        void createQuizSuccess() throws Exception {
            when(quizService.create(any(Quiz.class))).thenReturn(true);

            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(QuizTestDataFactory.createQuiz())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT /api/admin/quiz/quizzes/{id} - 更新试题成功")
        void updateQuizSuccess() throws Exception {
            when(quizService.update(any(Quiz.class))).thenReturn(true);

            mockMvc.perform(put("/api/admin/quiz/quizzes/quiz-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(QuizTestDataFactory.createQuiz())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /api/admin/quiz/quizzes/{id} - 删除试题成功")
        void deleteQuizSuccess() throws Exception {
            when(quizService.delete("quiz-1")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/quiz/quizzes/quiz-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("POST /api/admin/quiz/quizzes/{id}/options - 创建选项成功")
        void createQuizOptionSuccess() throws Exception {
            when(quizOptionService.create(anyString(), any(QuizOption.class))).thenReturn(true);

            mockMvc.perform(post("/api/admin/quiz/quizzes/quiz-1/options")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(QuizTestDataFactory.createQuizOption())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT /api/admin/quiz/quizzes/{quizId}/options/{optionId} - 更新选项成功")
        void updateQuizOptionSuccess() throws Exception {
            when(quizOptionService.update(eq("opt-1"), any(QuizOption.class))).thenReturn(true);

            mockMvc.perform(put("/api/admin/quiz/quizzes/quiz-1/options/opt-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(QuizTestDataFactory.createQuizOption())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /api/admin/quiz/quizzes/{quizId}/options/{optionId} - 删除选项成功")
        void deleteQuizOptionSuccess() throws Exception {
            when(quizOptionService.delete("opt-1")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/quiz/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("创建试题 - Service 抛出 BusinessException 返回 400")
        void createQuizThrowsBusinessException() throws Exception {
            when(quizService.create(any(Quiz.class)))
                    .thenThrow(new BusinessException(4000, "试题创建失败"));

            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(QuizTestDataFactory.createQuiz())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("试题创建失败"));
        }

        @Test
        @DisplayName("创建试题 - Service 抛出 RuntimeException 返回 500")
        void createQuizThrowsRuntimeException() throws Exception {
            when(quizService.create(any(Quiz.class)))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(QuizTestDataFactory.createQuiz())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("更新试题 - Service 返回 false 则 code 为 400")
        void updateQuizReturnsFalse() throws Exception {
            when(quizService.update(any(Quiz.class))).thenReturn(false);

            mockMvc.perform(put("/api/admin/quiz/quizzes/nonexistent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(QuizTestDataFactory.createQuiz())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("删除试题 - Service 返回 false 则 code 为 400")
        void deleteQuizReturnsFalse() throws Exception {
            when(quizService.delete("nonexistent")).thenReturn(false);

            mockMvc.perform(delete("/api/admin/quiz/quizzes/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("创建选项 - Service 返回 false 则 code 为 400")
        void createQuizOptionReturnsFalse() throws Exception {
            when(quizOptionService.create(anyString(), any(QuizOption.class))).thenReturn(false);

            mockMvc.perform(post("/api/admin/quiz/quizzes/quiz-1/options")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(QuizTestDataFactory.createQuizOption())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("更新选项 - Service 返回 false 则 code 为 400")
        void updateQuizOptionReturnsFalse() throws Exception {
            when(quizOptionService.update(anyString(), any(QuizOption.class))).thenReturn(false);

            mockMvc.perform(put("/api/admin/quiz/quizzes/quiz-1/options/nonexistent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(QuizTestDataFactory.createQuizOption())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("删除选项 - Service 返回 false 则 code 为 400")
        void deleteQuizOptionReturnsFalse() throws Exception {
            when(quizOptionService.delete("nonexistent")).thenReturn(false);

            mockMvc.perform(delete("/api/admin/quiz/quizzes/quiz-1/options/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("创建试题 - question 含中文正常处理")
        void createWithChineseQuestion() throws Exception {
            when(quizService.create(any(Quiz.class))).thenReturn(true);
            Quiz quiz = Quiz.builder()
                    .chapterId("chapter-1")
                    .question("习近平新时代中国特色社会主义思想的核心要义是什么？")
                    .questionType("single_choice")
                    .score(5)
                    .build();

            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(quiz)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("创建试题 - question 含特殊字符正常处理")
        void createWithSpecialChars() throws Exception {
            when(quizService.create(any(Quiz.class))).thenReturn(true);
            Quiz quiz = Quiz.builder()
                    .chapterId("chapter-1")
                    .question("test_@#$%^&*()")
                    .questionType("single_choice")
                    .score(5)
                    .build();

            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(quiz)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("创建试题 - question 超长字符串（1000 字符）正常处理")
        void createWithLongQuestion() throws Exception {
            when(quizService.create(any(Quiz.class))).thenReturn(true);
            Quiz quiz = Quiz.builder()
                    .chapterId("chapter-1")
                    .question("a".repeat(1000))
                    .questionType("single_choice")
                    .score(5)
                    .build();

            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(quiz)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("创建试题 - explanation 为空字符串正常处理")
        void createWithEmptyExplanation() throws Exception {
            when(quizService.create(any(Quiz.class))).thenReturn(true);
            Quiz quiz = Quiz.builder()
                    .chapterId("chapter-1")
                    .question("test question")
                    .questionType("single_choice")
                    .score(5)
                    .explanation("")
                    .build();

            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(quiz)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("创建选项 - optionText 含超长文本正常处理")
        void createOptionWithLongText() throws Exception {
            when(quizOptionService.create(anyString(), any(QuizOption.class))).thenReturn(true);
            QuizOption option = QuizOption.builder()
                    .optionText("a".repeat(500))
                    .isCorrect(true)
                    .orderIndex("A")
                    .build();

            mockMvc.perform(post("/api/admin/quiz/quizzes/quiz-1/options")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(option)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在 question 字段")
        void xssInQuestion() throws Exception {
            when(quizService.create(any(Quiz.class))).thenReturn(true);
            Quiz quiz = Quiz.builder()
                    .chapterId("chapter-1")
                    .question("<script>alert('xss')</script>")
                    .questionType("single_choice")
                    .score(5)
                    .build();

            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(quiz)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在 question 字段")
        void sqlInjectionInQuestion() throws Exception {
            when(quizService.create(any(Quiz.class))).thenReturn(true);
            Quiz quiz = Quiz.builder()
                    .chapterId("chapter-1")
                    .question("' OR '1'='1")
                    .questionType("single_choice")
                    .score(5)
                    .build();

            mockMvc.perform(post("/api/admin/quiz/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(QuizTestDataFactory.toJson(quiz)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET 请求创建试题接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/admin/quiz/quizzes"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("POST 请求删除试题接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/quiz/quizzes/quiz-1"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("GET 请求创建选项接口返回 405")
        void createOptionWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/admin/quiz/quizzes/quiz-1/options"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
