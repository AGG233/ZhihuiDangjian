package com.rauio.smartdangjian.controller.user;

import static org.mockito.ArgumentMatchers.any;
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
        public UserQuizAnswerController userQuizAnswerController(UserQuizAnswerService userQuizAnswerService) {
            return new UserQuizAnswerController(userQuizAnswerService);
        }
    }

    @MockitoBean
    private UserQuizAnswerService userQuizAnswerService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /api/quiz/answers/users/{id} - 获取用户全部答题记录成功")
        void getByUserIdSuccess() throws Exception {
            UserQuizAnswer answer1 = QuizTestDataFactory.createUserQuizAnswer("answer-1", "user-1", "quiz-1", "opt-1");
            UserQuizAnswer answer2 = QuizTestDataFactory.createUserQuizAnswer("answer-2", "user-1", "quiz-1", "opt-2");
            when(userQuizAnswerService.getByUserId("user-1")).thenReturn(List.of(answer1, answer2));

            mockMvc.perform(get("/api/quiz/answers/users/user-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].userId").value("user-1"))
                    .andExpect(jsonPath("$.data[1].userId").value("user-1"));
        }

        @Test
        @DisplayName("GET /api/quiz/answers/users/{id}/quizzes/{quizId} - 获取用户某题答题记录成功")
        void getByQuizIdSuccess() throws Exception {
            UserQuizAnswer answer1 = QuizTestDataFactory.createUserQuizAnswer("answer-1", "user-1", "quiz-1", "opt-1");
            when(userQuizAnswerService.getByUserIdAndQuizId("user-1", "quiz-1")).thenReturn(List.of(answer1));

            mockMvc.perform(get("/api/quiz/answers/users/user-1/quizzes/quiz-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].quizId").value("quiz-1"))
                    .andExpect(jsonPath("$.data[0].optionId").value("opt-1"));
        }

        @Test
        @DisplayName("GET /api/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId} - 获取指定答题记录成功")
        void getByOptionIdSuccess() throws Exception {
            UserQuizAnswer answer = QuizTestDataFactory.createUserQuizAnswer("answer-1", "user-1", "quiz-1", "opt-1");
            when(userQuizAnswerService.getByUserIdAndQuizIdAndOptionId("user-1", "quiz-1", "opt-1"))
                    .thenReturn(answer);

            mockMvc.perform(get("/api/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("answer-1"))
                    .andExpect(jsonPath("$.data.userId").value("user-1"))
                    .andExpect(jsonPath("$.data.quizId").value("quiz-1"))
                    .andExpect(jsonPath("$.data.optionId").value("opt-1"))
                    .andExpect(jsonPath("$.data.isCorrect").value(1))
                    .andExpect(jsonPath("$.data.scoreObtained").value(5));
        }

        @Test
        @DisplayName("POST /api/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId} - 提交答题成功")
        void createQuizAnswerSuccess() throws Exception {
            when(userQuizAnswerService.create(any(UserQuizAnswer.class))).thenReturn(true);

            mockMvc.perform(post("/api/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT /api/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId} - 更新答题成功")
        void updateQuizAnswerSuccess() throws Exception {
            when(userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(any(UserQuizAnswer.class)))
                    .thenReturn(true);

            mockMvc.perform(put("/api/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /users/{id} - Service 抛出 BusinessException 返回 400")
        void getByUserIdThrowsBusinessException() throws Exception {
            when(userQuizAnswerService.getByUserId("user-1")).thenThrow(new BusinessException(4001, "用户不存在"));

            mockMvc.perform(get("/api/quiz/answers/users/user-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("GET /users/{id}/quizzes/{quizId} - Service 抛出 RuntimeException 返回 500")
        void getByQuizIdThrowsRuntimeException() throws Exception {
            when(userQuizAnswerService.getByUserIdAndQuizId(anyString(), anyString()))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/quiz/answers/users/user-1/quizzes/quiz-1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /users/{id}/quizzes/{quizId}/options/{optionId} - 记录不存在返回 null 则 code 为 400")
        void getByOptionIdReturnsNull() throws Exception {
            when(userQuizAnswerService.getByUserIdAndQuizIdAndOptionId("user-1", "quiz-1", "opt-none"))
                    .thenReturn(null);

            mockMvc.perform(get("/api/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-none"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("POST - 提交答题 Service 返回 false 则 code 为 400")
        void createQuizAnswerReturnsFalse() throws Exception {
            when(userQuizAnswerService.create(any(UserQuizAnswer.class))).thenReturn(false);

            mockMvc.perform(post("/api/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
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

            mockMvc.perform(put("/api/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("POST - Service 抛出 BusinessException 返回 400")
        void createQuizAnswerThrowsBusinessException() throws Exception {
            when(userQuizAnswerService.create(any(UserQuizAnswer.class)))
                    .thenThrow(new BusinessException(4000, "答题提交失败"));

            mockMvc.perform(post("/api/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("答题提交失败"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /users/{id} - 无答题记录返回空列表")
        void getByUserIdEmpty() throws Exception {
            when(userQuizAnswerService.getByUserId("user-empty")).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/answers/users/user-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /users/{id}/quizzes/{quizId} - 某题无答题记录返回空列表")
        void getByQuizIdEmpty() throws Exception {
            when(userQuizAnswerService.getByUserIdAndQuizId("user-1", "quiz-empty"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/answers/users/user-1/quizzes/quiz-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("路径参数含中文正常处理")
        void withChinesePathParams() throws Exception {
            UserQuizAnswer answer = QuizTestDataFactory.createUserQuizAnswer("answer-1", "用户1", "quiz-1", "opt-1");
            when(userQuizAnswerService.getByUserId("用户1")).thenReturn(List.of(answer));

            mockMvc.perform(get("/api/quiz/answers/users/用户1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("路径含特殊字符正常处理")
        void withSpecialCharsInPath() throws Exception {
            when(userQuizAnswerService.getByUserId("test@#$%")).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/answers/users/test@#$%"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("超长路径参数正常处理")
        void withLongPathParams() throws Exception {
            String longId = "a".repeat(500);
            when(userQuizAnswerService.getByUserId(longId)).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/answers/users/" + longId))
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
            when(userQuizAnswerService.getByUserId("<script>alert('xss')</script>"))
                    .thenReturn(null);

            mockMvc.perform(get("/api/quiz/answers/users/%3Cscript%3Ealert('xss')%3C%2Fscript%3E"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入尝试在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(userQuizAnswerService.getByUserId("' OR '1'='1")).thenReturn(List.of());

            mockMvc.perform(get("/api/quiz/answers/users/{id}", "' OR '1'='1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST 请求获取答题记录接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/quiz/answers/users/user-1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求获取答题记录接口返回 405")
        void getByUserIdWithWrongMethod() throws Exception {
            mockMvc.perform(put("/api/quiz/answers/users/user-1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求获取答题记录接口返回 405")
        void getByUserIdWithDeleteMethod() throws Exception {
            mockMvc.perform(delete("/api/quiz/answers/users/user-1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("GET 请求提交答题接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk());
        }
    }
}
