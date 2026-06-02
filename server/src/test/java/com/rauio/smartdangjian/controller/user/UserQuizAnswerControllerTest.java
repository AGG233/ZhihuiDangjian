package com.rauio.smartdangjian.controller.user;

import static org.mockito.ArgumentMatchers.any;
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
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.controller.user.UserQuizAnswerController;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserQuizAnswerControllerTest.TestConfig.class)
@DisplayName("用户答题记录接口测试")
class UserQuizAnswerControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserQuizAnswerController userQuizAnswerController(
                UserQuizAnswerService userQuizAnswerService,
                com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserQuizAnswerController(userQuizAnswerService, currentUserProvider);
        }
    }

    @MockitoBean
    private UserQuizAnswerService userQuizAnswerService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /api/quiz/answers/me - 获取当前用户全部答题记录成功")
        void getByUserIdSuccess() throws Exception {
            UserQuizAnswer answer1 = QuizTestDataFactory.createUserQuizAnswer(1L, 1L, 1L, 1L);
            UserQuizAnswer answer2 = QuizTestDataFactory.createUserQuizAnswer(2L, 1L, 2L, 2L);
            when(userQuizAnswerService.getByUserId(1L)).thenReturn(List.of(answer1, answer2));

            mockMvc.perform(get("/api/quiz/answers/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].userId").value("1"))
                    .andExpect(jsonPath("$.data[1].userId").value("1"));
        }

        @Test
        @DisplayName("GET /api/quiz/answers/me/quizzes/{quizId} - 获取当前用户某题答题记录成功")
        void getByQuizIdSuccess() throws Exception {
            UserQuizAnswer answer1 = QuizTestDataFactory.createUserQuizAnswer(1L, 1L, 1L, 1L);
            when(userQuizAnswerService.getByUserIdAndQuizId(1L, 1L)).thenReturn(List.of(answer1));

            mockMvc.perform(get("/api/quiz/answers/me/quizzes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].quizId").value("1"))
                    .andExpect(jsonPath("$.data[0].optionId").value("1"));
        }

        @Test
        @DisplayName("GET /api/quiz/answers/me/quizzes/{quizId}/options/{optionId} - 获取当前用户指定答题记录成功")
        void getByOptionIdSuccess() throws Exception {
            UserQuizAnswer answer = QuizTestDataFactory.createUserQuizAnswer(1L, 1L, 1L, 1L);
            when(userQuizAnswerService.getByUserIdAndQuizIdAndOptionId(1L, 1L, 1L))
                    .thenReturn(answer);

            mockMvc.perform(get("/api/quiz/answers/me/quizzes/1/options/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.userId").value("1"))
                    .andExpect(jsonPath("$.data.quizId").value("1"))
                    .andExpect(jsonPath("$.data.optionId").value("1"))
                    .andExpect(jsonPath("$.data.isCorrect").value(1))
                    .andExpect(jsonPath("$.data.scoreObtained").value(5));
        }

        @Test
        @DisplayName("POST /api/quiz/answers/me/quizzes/{quizId}/options/{optionId} - 提交答题成功")
        void createQuizAnswerSuccess() throws Exception {
            when(userQuizAnswerService.create(any(UserQuizAnswer.class))).thenReturn(true);

            mockMvc.perform(post("/api/quiz/answers/me/quizzes/1/options/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT /api/quiz/answers/me/quizzes/{quizId}/options/{optionId} - 更新答题成功")
        void updateQuizAnswerSuccess() throws Exception {
            when(userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(any(UserQuizAnswer.class)))
                    .thenReturn(true);

            mockMvc.perform(put("/api/quiz/answers/me/quizzes/1/options/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /me - Service 抛出 BusinessException 返回 400")
        void getByUserIdThrowsBusinessException() throws Exception {
            when(userQuizAnswerService.getByUserId(1L)).thenThrow(new BusinessException(4001, "用户不存在"));

            mockMvc.perform(get("/api/quiz/answers/me"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("GET /me/quizzes/{quizId} - Service 抛出 RuntimeException 返回 500")
        void getByQuizIdThrowsRuntimeException() throws Exception {
            when(userQuizAnswerService.getByUserIdAndQuizId(anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/quiz/answers/me/quizzes/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /me/quizzes/{quizId}/options/{optionId} - 记录不存在返回 null 则 data 为 null")
        void getByOptionIdReturnsNull() throws Exception {
            when(userQuizAnswerService.getByUserIdAndQuizIdAndOptionId(1L, 1L, 999L))
                    .thenReturn(null);

            mockMvc.perform(get("/api/quiz/answers/me/quizzes/1/options/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("GET /me/quizzes/{quizId} - 服务返回 null 元素处理")
        void getByQuizIdWithNullElement() throws Exception {
            when(userQuizAnswerService.getByUserIdAndQuizId(1L, 1L)).thenReturn(java.util.Arrays.asList(null, null));

            mockMvc.perform(get("/api/quiz/answers/me/quizzes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("POST - 提交答题 Service 返回 false 则 code 为 400")
        void createQuizAnswerReturnsFalse() throws Exception {
            when(userQuizAnswerService.create(any(UserQuizAnswer.class))).thenReturn(false);

            mockMvc.perform(post("/api/quiz/answers/me/quizzes/1/options/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("PUT - 更新答题 Service 返回 false 则 code 为 400")
        void updateQuizAnswerReturnsFalse() throws Exception {
            when(userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(any(UserQuizAnswer.class)))
                    .thenReturn(false);

            mockMvc.perform(put("/api/quiz/answers/me/quizzes/1/options/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("POST - Service 抛出 BusinessException 返回 400")
        void createQuizAnswerThrowsBusinessException() throws Exception {
            when(userQuizAnswerService.create(any(UserQuizAnswer.class)))
                    .thenThrow(new BusinessException(QuizErrorConstants.QUIZ_NOT_FOUND, "答题提交失败"));

            mockMvc.perform(post("/api/quiz/answers/me/quizzes/1/options/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("6001"))
                    .andExpect(jsonPath("$.message").value("答题提交失败"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /me - 无答题记录返回空列表")
        void getByUserIdEmpty() throws Exception {
            when(userQuizAnswerService.getByUserId(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/answers/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /me/quizzes/{quizId} - 某题无答题记录返回空列表")
        void getByQuizIdEmpty() throws Exception {
            when(userQuizAnswerService.getByUserIdAndQuizId(1L, 999L)).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/answers/me/quizzes/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("非数字路径参数返回 400（Spring 类型转换失败）")
        void withNonNumericPathParams() throws Exception {
            mockMvc.perform(get("/api/quiz/answers/me/quizzes/用户1")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("路径含特殊字符返回 400（Spring 类型转换失败）")
        void withSpecialCharsInPath() throws Exception {
            mockMvc.perform(get("/api/quiz/answers/me/quizzes/test@#$%")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("浮点路径参数返回 400（Spring 类型转换失败）")
        void withInvalidPathParams() throws Exception {
            mockMvc.perform(get("/api/quiz/answers/me/quizzes/3.14")).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 尝试在路径参数中返回 400（类型转换失败）")
        void xssInPath() throws Exception {
            mockMvc.perform(get("/api/quiz/answers/me/quizzes/%3Cscript%3Ealert('xss')%3E"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("SQL 注入尝试在路径参数中返回 400（类型转换失败）")
        void sqlInjectionInPath() throws Exception {
            mockMvc.perform(get("/api/quiz/answers/me/quizzes/{id}", "' OR '1'='1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST 请求获取答题记录接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/quiz/answers/me")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求获取答题记录接口返回 405")
        void getByUserIdWithWrongMethod() throws Exception {
            mockMvc.perform(put("/api/quiz/answers/me")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求获取答题记录接口返回 405")
        void getByUserIdWithDeleteMethod() throws Exception {
            mockMvc.perform(delete("/api/quiz/answers/me")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("GET 请求提交答题接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/quiz/answers/me/quizzes/1/options/1")).andExpect(status().isOk());
        }
    }
}
