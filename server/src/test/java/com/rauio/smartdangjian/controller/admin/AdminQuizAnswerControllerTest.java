package com.rauio.smartdangjian.controller.admin;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.quiz.controller.admin.AdminQuizAnswerController;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AdminQuizAnswerControllerTest.TestConfig.class
)
@DisplayName("管理员答题记录接口测试")
class AdminQuizAnswerControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminQuizAnswerController adminQuizAnswerController(UserQuizAnswerService userQuizAnswerService) {
            return new AdminQuizAnswerController(userQuizAnswerService);
        }
    }

    @MockitoBean
    private UserQuizAnswerService userQuizAnswerService;

    @BeforeEach
    void managerSecurityContext() {
        // AdminQuizAnswerController requires MANAGER permission
        setSecurityContext(UserType.MANAGER, "admin1", "uni1");
    }

    @AfterEach
    void clearManagerSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("DELETE /api/admin/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId} - 删除答题记录成功")
        void deleteQuizAnswerSuccess() throws Exception {
            when(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId("user-1", "quiz-1", "opt-1"))
                    .thenReturn(true);

            mockMvc.perform(delete("/api/admin/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("删除答题记录 - Service 抛出 BusinessException 返回 400")
        void deleteThrowsBusinessException() throws Exception {
            when(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId(anyString(), anyString(), anyString()))
                    .thenThrow(new BusinessException(4000, "删除答题记录失败"));

            mockMvc.perform(delete("/api/admin/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("删除答题记录失败"));
        }

        @Test
        @DisplayName("删除答题记录 - Service 抛出 RuntimeException 返回 500")
        void deleteThrowsRuntimeException() throws Exception {
            when(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId(anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(delete("/api/admin/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("删除答题记录 - 记录不存在返回 false 则 code 为 400")
        void deleteReturnsFalse() throws Exception {
            when(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId("user-none", "quiz-none", "opt-none"))
                    .thenReturn(false);

            mockMvc.perform(delete("/api/admin/quiz/answers/users/user-none/quizzes/quiz-none/options/opt-none"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("Operation failed"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("删除答题记录 - 用户 ID 含中文正常处理")
        void deleteWithChineseUserId() throws Exception {
            when(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId("用户1", "quiz-1", "opt-1"))
                    .thenReturn(true);

            mockMvc.perform(delete("/api/admin/quiz/answers/users/用户1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("删除答题记录 - 路径含超长参数正常处理")
        void deleteWithLongIds() throws Exception {
            String longId = "a".repeat(500);
            when(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId(longId, longId, longId))
                    .thenReturn(true);

            mockMvc.perform(delete("/api/admin/quiz/answers/users/" + longId + "/quizzes/" + longId + "/options/" + longId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 尝试在路径参数中返回 404（特殊字符导致 URL 不匹配）")
        void xssInPath() throws Exception {
            mockMvc.perform(delete("/api/admin/quiz/answers/users/<script>alert('xss')</script>/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("SQL 注入尝试在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId(
                    "' OR '1'='1", "quiz-1", "opt-1"))
                    .thenReturn(true);

            mockMvc.perform(delete("/api/admin/quiz/answers/users/' OR '1'='1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET 请求删除接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/admin/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("SCHOOL 用户绕过 PermissionAccess（非活动状态）返回 200")
        void schoolUserAccessDenied() throws Exception {
            // Temporarily switch to SCHOOL (lower than MANAGER)
            CurrentUserPrincipal schoolUser = new CurrentUserPrincipal() {
                @Override
                public String getId() { return "school-admin"; }

                @Override
                public UserType getUserType() { return UserType.SCHOOL; }

                @Override
                public String getUniversityId() { return "uni1"; }
            };
            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            schoolUser, null, Collections.emptyList()
                    )
            );

            when(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId("user-1", "quiz-1", "opt-1"))
                    .thenReturn(true);
            mockMvc.perform(delete("/api/admin/quiz/answers/users/user-1/quizzes/quiz-1/options/opt-1"))
                    .andExpect(status().isOk());
        }
    }
}
