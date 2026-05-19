package com.rauio.smartdangjian.controller.user;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.QuizTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.controller.user.UserQuizController;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = UserQuizControllerTest.TestConfig.class)
@DisplayName("用户试题接口测试")
class UserQuizControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserQuizController userQuizController(QuizService quizService, QuizOptionService quizOptionService) {
            return new UserQuizController(quizService, quizOptionService);
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
        @DisplayName("GET /api/quiz/quizzes/{id} - 获取试题详情成功")
        void getQuizSuccess() throws Exception {
            Quiz quiz = QuizTestDataFactory.createQuiz();
            when(quizService.get("quiz-1")).thenReturn(quiz);

            mockMvc.perform(get("/api/quiz/quizzes/quiz-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("quiz-1"))
                    .andExpect(jsonPath("$.data.chapterId").value("chapter-1"))
                    .andExpect(jsonPath("$.data.questionType").value("single_choice"))
                    .andExpect(jsonPath("$.data.difficulty").value("medium"))
                    .andExpect(jsonPath("$.data.isActive").value(true));
        }

        @Test
        @DisplayName("GET /api/quiz/quizzes/by-chapter/{chapterId} - 获取章节下所有试题成功")
        void getQuizOfChapterSuccess() throws Exception {
            Quiz quiz1 = QuizTestDataFactory.createQuiz("quiz-1");
            Quiz quiz2 = QuizTestDataFactory.createQuiz("quiz-2");
            when(quizService.getByChapterId("chapter-1")).thenReturn(List.of(quiz1, quiz2));

            mockMvc.perform(get("/api/quiz/quizzes/by-chapter/chapter-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value("quiz-1"))
                    .andExpect(jsonPath("$.data[1].id").value("quiz-2"));
        }

        @Test
        @DisplayName("GET /api/quiz/quizzes/{id}/options - 获取试题选项列表成功")
        void getQuizOptionsSuccess() throws Exception {
            QuizOption opt1 = QuizTestDataFactory.createQuizOption("opt-1", "quiz-1", "选项A", true, "A");
            QuizOption opt2 = QuizTestDataFactory.createQuizOption("opt-2", "quiz-1", "选项B", false, "B");
            when(quizOptionService.getByQuizId("quiz-1")).thenReturn(List.of(opt1, opt2));

            mockMvc.perform(get("/api/quiz/quizzes/quiz-1/options"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value("opt-1"))
                    .andExpect(jsonPath("$.data[1].id").value("opt-2"));
        }

        @Test
        @DisplayName("GET /api/quiz/quizzes/{id}/options/{optionId} - 获取单个选项详情成功")
        void getByOptionIdSuccess() throws Exception {
            QuizOption option = QuizTestDataFactory.createQuizOption();
            when(quizOptionService.get("opt-1")).thenReturn(option);

            mockMvc.perform(get("/api/quiz/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("opt-1"))
                    .andExpect(jsonPath("$.data.quizId").value("quiz-1"))
                    .andExpect(jsonPath("$.data.optionText").value("实现共产主义"))
                    .andExpect(jsonPath("$.data.orderIndex").value("A"));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /{id} - 试题不存在返回 null 时 code 为 400")
        void getQuizReturnsNull() throws Exception {
            when(quizService.get("nonexistent")).thenReturn(null);

            mockMvc.perform(get("/api/quiz/quizzes/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 BusinessException 返回 400")
        void getQuizThrowsBusinessException() throws Exception {
            when(quizService.get("quiz-1")).thenThrow(new BusinessException(4001, "试题不存在"));

            mockMvc.perform(get("/api/quiz/quizzes/quiz-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("试题不存在"));
        }

        @Test
        @DisplayName("GET /by-chapter/{chapterId} - Service 抛出 RuntimeException 返回 500")
        void getQuizOfChapterThrowsRuntimeException() throws Exception {
            when(quizService.getByChapterId(anyString())).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/quiz/quizzes/by-chapter/chapter-1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /{id}/options - 选项列表为空返回空数组")
        void getQuizOptionsReturnsEmpty() throws Exception {
            when(quizOptionService.getByQuizId("quiz-1")).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/quizzes/quiz-1/options"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id}/options/{optionId} - 选项不存在返回 null")
        void getByOptionIdReturnsNull() throws Exception {
            when(quizOptionService.get("nonexistent")).thenReturn(null);

            mockMvc.perform(get("/api/quiz/quizzes/quiz-1/options/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("GET /{id}/options/{optionId} - Service 抛出 BusinessException 返回 400")
        void getByOptionIdThrowsBusinessException() throws Exception {
            when(quizOptionService.get("opt-1")).thenThrow(new BusinessException(4001, "选项不存在"));

            mockMvc.perform(get("/api/quiz/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("选项不存在"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /by-chapter/{chapterId} - 章节下无试题返回空列表")
        void getQuizOfChapterEmpty() throws Exception {
            when(quizService.getByChapterId("chapter-empty")).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/quizzes/by-chapter/chapter-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id} - 路径含中文正常处理")
        void getWithChineseId() throws Exception {
            Quiz quiz = QuizTestDataFactory.createQuiz("q-1");
            when(quizService.get("试题")).thenReturn(quiz);

            mockMvc.perform(get("/api/quiz/quizzes/试题"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("GET /{id} - 路径含特殊字符正常处理")
        void getWithSpecialCharsInPath() throws Exception {
            Quiz quiz = QuizTestDataFactory.createQuiz("q-1");
            when(quizService.get("test@#$%")).thenReturn(quiz);

            mockMvc.perform(get("/api/quiz/quizzes/{id}", "test@#$%"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("GET /by-chapter/{chapterId} - 超长 chapterId 正常处理")
        void getByChapterWithLongId() throws Exception {
            String longId = "a".repeat(500);
            when(quizService.getByChapterId(longId)).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/quizzes/by-chapter/" + longId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 尝试在路径参数中")
        void xssInPath() throws Exception {
            when(quizService.get("<script>alert('xss')</script>")).thenReturn(null);

            mockMvc.perform(get("/api/quiz/quizzes/%3Cscript%3Ealert('xss')%3C%2Fscript%3E"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入尝试在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(quizService.get("' OR '1'='1")).thenReturn(null);

            mockMvc.perform(get("/api/quiz/quizzes/{id}", "' OR '1'='1")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST 请求获取试题详情接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/quiz/quizzes/quiz-1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求获取章节试题接口返回 405")
        void getByChapterWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/quiz/quizzes/by-chapter/chapter-1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求获取选项列表接口返回 405")
        void getOptionsWithWrongMethod() throws Exception {
            mockMvc.perform(put("/api/quiz/quizzes/quiz-1/options")).andExpect(status().isMethodNotAllowed());
        }
    }
}
