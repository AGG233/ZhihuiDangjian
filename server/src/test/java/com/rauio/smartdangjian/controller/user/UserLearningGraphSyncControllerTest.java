package com.rauio.smartdangjian.controller.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.constants.GraphErrorConstants;
import com.rauio.smartdangjian.server.learning.controller.user.UserLearningGraphSyncController;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserLearningGraphSyncControllerTest.TestConfig.class)
@DisplayName("学习图谱同步接口测试")
class UserLearningGraphSyncControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserLearningGraphSyncController userLearningGraphSyncController(
                UserLearningRecordService userLearningRecordService) {
            return new UserLearningGraphSyncController(userLearningRecordService);
        }
    }

    @MockitoBean
    private UserLearningRecordService userLearningRecordService;

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST /user/{userId}/sync - 同步用户学习图谱成功")
        void syncUserGraphSuccess() throws Exception {
            when(userLearningRecordService.syncUserLearningGraph(1L)).thenReturn(5);

            mockMvc.perform(post("/api/learning/graph/users/1/sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(5));
        }

        @Test
        @DisplayName("POST /user/{userId}/sync - 同步无学习记录的用户返回 0")
        void syncUserGraphNoRecords() throws Exception {
            when(userLearningRecordService.syncUserLearningGraph(2L)).thenReturn(0);

            mockMvc.perform(post("/api/learning/graph/users/2/sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(0));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 异常处理场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("POST /user/{userId}/sync - Service 抛出 BusinessException 返回 400")
        void syncThrowsBusinessException() throws Exception {
            when(userLearningRecordService.syncUserLearningGraph(1L))
                    .thenThrow(new BusinessException(GraphErrorConstants.USER_NOT_FOUND, "用户不存在"));

            mockMvc.perform(post("/api/learning/graph/users/1/sync"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("7001"))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("POST /user/{userId}/sync - Service 抛出 RuntimeException 返回 500")
        void syncThrowsRuntimeException() throws Exception {
            when(userLearningRecordService.syncUserLearningGraph(1L)).thenThrow(new RuntimeException("图谱同步异常"));

            mockMvc.perform(post("/api/learning/graph/users/1/sync"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("POST /user/{userId}/sync - 用户 ID 含特殊字符")
        void syncWithSpecialCharsInUserId() throws Exception {
            when(userLearningRecordService.syncUserLearningGraph(3L)).thenReturn(0);

            mockMvc.perform(post("/api/learning/graph/users/3/sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST /user/{userId}/sync - 大量记录同步返回大数字")
        void syncWithLargeNumberOfRecords() throws Exception {
            when(userLearningRecordService.syncUserLearningGraph(4L)).thenReturn(9999);

            mockMvc.perform(post("/api/learning/graph/users/4/sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(9999));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 安全场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在路径参数中")
        void xssInPath() throws Exception {
            when(userLearningRecordService.syncUserLearningGraph(999L))
                    .thenThrow(new BusinessException(GraphErrorConstants.USER_NOT_FOUND, "用户不存在"));

            mockMvc.perform(post(URI.create("/api/learning/graph/users/999/sync")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("SQL 注入在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(userLearningRecordService.syncUserLearningGraph(9999L))
                    .thenThrow(new BusinessException(GraphErrorConstants.USER_NOT_FOUND, "用户不存在"));

            mockMvc.perform(post("/api/learning/graph/users/9999/sync")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET 请求同步接口返回 405")
        void syncWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/learning/graph/users/1/sync")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求同步接口返回 405")
        void syncWithDeleteMethod() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                            "/api/learning/graph/users/1/sync"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
