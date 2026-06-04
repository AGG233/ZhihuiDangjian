package com.rauio.smartdangjian.controller.user;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.ControllerTestConfiguration;
import com.rauio.smartdangjian.controller.factory.QuizTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ControllerTestConfiguration.class)
@DisplayName("用户试题接口测试")
class UserQuizControllerTest extends BaseControllerTest {

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

        @ParameterizedTest(name = "quiz id={0}")
        @ValueSource(strings = {"试题", "test@#$%", "3.14", "<script>alert('xss')", "' OR '1'='1"})
        @DisplayName("GET /{id} - 非法试题 ID 返回 400 且不调用业务服务")
        void getWithInvalidQuizId(String id) throws Exception {
            mockMvc.perform(get("/api/quiz/quizzes/{id}", id)).andExpect(status().isBadRequest());

            verify(quizService, never()).get(anyLong());
        }

        @ParameterizedTest(name = "chapter id={0}")
        @ValueSource(strings = {"章节", "test@#$%", "3.14", "<script>alert('xss')", "' OR '1'='1"})
        @DisplayName("GET /by-chapter/{chapterId} - 非法章节 ID 返回 400 且不调用业务服务")
        void getByChapterWithInvalidId(String chapterId) throws Exception {
            mockMvc.perform(get("/api/quiz/quizzes/by-chapter/{chapterId}", chapterId))
                    .andExpect(status().isBadRequest());

            verify(quizService, never()).getByChapterId(anyLong());
        }

        @ParameterizedTest(name = "quiz id={0}")
        @ValueSource(strings = {"选项", "test@#$%", "3.14", "<script>alert('xss')", "' OR '1'='1"})
        @DisplayName("GET /{id}/options - 非法试题 ID 返回 400 且不查询选项")
        void getOptionsWithInvalidQuizId(String id) throws Exception {
            mockMvc.perform(get("/api/quiz/quizzes/{id}/options", id)).andExpect(status().isBadRequest());

            verify(quizOptionService, never()).getByQuizId(anyLong());
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @ParameterizedTest(name = "{0} {1}")
        @CsvSource({
            "POST,/api/quiz/quizzes/1",
            "DELETE,/api/quiz/quizzes/by-chapter/1",
            "PUT,/api/quiz/quizzes/1/options",
            "PATCH,/api/quiz/quizzes/1/options/1"
        })
        @DisplayName("只读接口使用错误 HTTP 方法返回 405")
        void readEndpointsWithWrongMethod(String method, String path) throws Exception {
            mockMvc.perform(request(method, path)).andExpect(status().isMethodNotAllowed());
        }

        private MockHttpServletRequestBuilder request(String method, String path) {
            return switch (method) {
                case "POST" -> post(path);
                case "DELETE" -> delete(path);
                case "PUT" -> put(path);
                case "PATCH" -> patch(path);
                default -> throw new IllegalArgumentException("Unsupported method: " + method);
            };
        }
    }
}
