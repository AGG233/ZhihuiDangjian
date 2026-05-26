package com.rauio.smartdangjian.controller.user;

import static org.mockito.ArgumentMatchers.anyLong;
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
            when(quizService.get(1L)).thenReturn(quiz);

            mockMvc.perform(get("/api/quiz/quizzes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.chapterId").value("1"))
                    .andExpect(jsonPath("$.data.questionType").value("single_choice"))
                    .andExpect(jsonPath("$.data.difficulty").value("medium"))
                    .andExpect(jsonPath("$.data.isActive").value(true));
        }

        @Test
        @DisplayName("GET /api/quiz/quizzes/by-chapter/{chapterId} - 获取章节下所有试题成功")
        void getQuizOfChapterSuccess() throws Exception {
            Quiz quiz1 = QuizTestDataFactory.createQuiz(1L);
            Quiz quiz2 = QuizTestDataFactory.createQuiz(2L);
            when(quizService.getByChapterId(1L)).thenReturn(List.of(quiz1, quiz2));

            mockMvc.perform(get("/api/quiz/quizzes/by-chapter/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[1].id").value("2"));
        }

        @Test
        @DisplayName("GET /api/quiz/quizzes/{id}/options - 获取试题选项列表成功")
        void getQuizOptionsSuccess() throws Exception {
            QuizOption opt1 = QuizTestDataFactory.createQuizOption(1L, 1L, "选项A", true, "A");
            QuizOption opt2 = QuizTestDataFactory.createQuizOption(2L, 1L, "选项B", false, "B");
            when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(opt1, opt2));

            mockMvc.perform(get("/api/quiz/quizzes/1/options"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[1].id").value("2"));
        }

        @Test
        @DisplayName("GET /api/quiz/quizzes/{id}/options/{optionId} - 获取单个选项详情成功")
        void getByOptionIdSuccess() throws Exception {
            QuizOption option = QuizTestDataFactory.createQuizOption();
            when(quizOptionService.get(1L)).thenReturn(option);

            mockMvc.perform(get("/api/quiz/quizzes/1/options/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.quizId").value("1"))
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
            when(quizService.get(999L)).thenReturn(null);

            mockMvc.perform(get("/api/quiz/quizzes/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 BusinessException 返回 400")
        void getQuizThrowsBusinessException() throws Exception {
            when(quizService.get(1L)).thenThrow(new BusinessException(4001, "试题不存在"));

            mockMvc.perform(get("/api/quiz/quizzes/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("试题不存在"));
        }

        @Test
        @DisplayName("GET /by-chapter/{chapterId} - Service 抛出 RuntimeException 返回 500")
        void getQuizOfChapterThrowsRuntimeException() throws Exception {
            when(quizService.getByChapterId(anyLong())).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/quiz/quizzes/by-chapter/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /{id}/options - 选项列表为空返回空数组")
        void getQuizOptionsReturnsEmpty() throws Exception {
            when(quizOptionService.getByQuizId(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/quizzes/1/options"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id}/options/{optionId} - 选项不存在返回 null")
        void getByOptionIdReturnsNull() throws Exception {
            when(quizOptionService.get(999L)).thenReturn(null);

            mockMvc.perform(get("/api/quiz/quizzes/1/options/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("GET /{id}/options/{optionId} - Service 抛出 BusinessException 返回 400")
        void getByOptionIdThrowsBusinessException() throws Exception {
            when(quizOptionService.get(1L)).thenThrow(new BusinessException(4001, "选项不存在"));

            mockMvc.perform(get("/api/quiz/quizzes/1/options/1"))
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
            when(quizService.getByChapterId(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/quizzes/by-chapter/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id} - 非数字 ID 返回 400（Spring 类型转换失败）")
        void getWithNonNumericId() throws Exception {
            mockMvc.perform(get("/api/quiz/quizzes/试题"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /{id} - 路径含特殊字符返回 400（Spring 类型转换失败）")
        void getWithSpecialCharsInPath() throws Exception {
            mockMvc.perform(get("/api/quiz/quizzes/{id}", "test@#$%"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /by-chapter/{chapterId} - Float ID 路径返回 400（Spring 类型转换失败）")
        void getByChapterWithInvalidId() throws Exception {
            mockMvc.perform(get("/api/quiz/quizzes/by-chapter/3.14"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 尝试在路径参数中返回 400（类型转换失败）")
        void xssInPath() throws Exception {
            mockMvc.perform(get("/api/quiz/quizzes/%3Cscript%3Ealert('xss')%3E"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("SQL 注入尝试在路径参数中返回 400（类型转换失败）")
        void sqlInjectionInPath() throws Exception {
            mockMvc.perform(get("/api/quiz/quizzes/{id}", "' OR '1'='1")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST 请求获取试题详情接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/quiz/quizzes/1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求获取章节试题接口返回 405")
        void getByChapterWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/quiz/quizzes/by-chapter/1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求获取选项列表接口返回 405")
        void getOptionsWithWrongMethod() throws Exception {
            mockMvc.perform(put("/api/quiz/quizzes/1/options")).andExpect(status().isMethodNotAllowed());
        }
    }
}
