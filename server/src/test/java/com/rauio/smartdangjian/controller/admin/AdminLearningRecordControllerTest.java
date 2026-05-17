package com.rauio.smartdangjian.controller.admin;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.LearningTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
import com.rauio.smartdangjian.server.learning.controller.admin.AdminLearningRecordController;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AdminLearningRecordControllerTest.TestConfig.class)
@DisplayName("管理员学习记录接口测试")
class AdminLearningRecordControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminLearningRecordController adminLearningRecordController(UserLearningRecordService recordService) {
            return new AdminLearningRecordController(recordService);
        }
    }

    @MockitoBean
    private UserLearningRecordService recordService;

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /chapter/{chapterId} - 获取章节所有学习记录成功")
        void getByChapterIdSuccess() throws Exception {
            UserLearningRecordVO vo = LearningTestDataFactory.createLearningRecordVO("rec-001");
            when(recordService.getByChapterId("ch-001")).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/admin/learning/records/chapter/ch-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].id").value("rec-001"))
                    .andExpect(jsonPath("$.data[0].userId").value("user-001"))
                    .andExpect(jsonPath("$.data[0].chapterId").value("ch-001"));
        }

        @Test
        @DisplayName("DELETE /{id} - 删除学习记录成功")
        void deleteSuccess() throws Exception {
            when(recordService.delete("rec-001")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/learning/records/rec-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 异常处理场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /chapter/{chapterId} - Service 抛出 BusinessException 返回 400")
        void getByChapterIdThrowsBusinessException() throws Exception {
            when(recordService.getByChapterId("ch-001"))
                    .thenThrow(new BusinessException(4000, "章节不存在"));

            mockMvc.perform(get("/api/admin/learning/records/chapter/ch-001"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("章节不存在"));
        }

        @Test
        @DisplayName("DELETE /{id} - Service 抛出 BusinessException 返回 400")
        void deleteThrowsBusinessException() throws Exception {
            when(recordService.delete("nonexistent"))
                    .thenThrow(new BusinessException(4000, "学习记录不存在"));

            mockMvc.perform(delete("/api/admin/learning/records/nonexistent"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("学习记录不存在"));
        }

        @Test
        @DisplayName("GET /chapter/{chapterId} - Service 抛出 RuntimeException 返回 500")
        void getByChapterIdThrowsRuntimeException() throws Exception {
            when(recordService.getByChapterId("ch-001"))
                    .thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/admin/learning/records/chapter/ch-001"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("DELETE /{id} - Service 抛出 RuntimeException 返回 500")
        void deleteThrowsRuntimeException() throws Exception {
            when(recordService.delete("rec-001"))
                    .thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(delete("/api/admin/learning/records/rec-001"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("DELETE /{id} - Service 返回 false 时 code 为 400")
        void deleteReturnsFalse() throws Exception {
            when(recordService.delete("nonexistent")).thenReturn(false);

            mockMvc.perform(delete("/api/admin/learning/records/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /chapter/{chapterId} - 空结果集返回空列表")
        void getByChapterIdEmptyResult() throws Exception {
            when(recordService.getByChapterId("ch-empty")).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/learning/records/chapter/ch-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /chapter/{chapterId} - 多记录返回")
        void getByChapterIdMultipleRecords() throws Exception {
            List<UserLearningRecordVO> list = List.of(
                    LearningTestDataFactory.createLearningRecordVO("rec-001", "user-001", "ch-001"),
                    LearningTestDataFactory.createLearningRecordVO("rec-002", "user-002", "ch-001"),
                    LearningTestDataFactory.createLearningRecordVO("rec-003", "user-003", "ch-001")
            );
            when(recordService.getByChapterId("ch-001")).thenReturn(list);

            mockMvc.perform(get("/api/admin/learning/records/chapter/ch-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(3));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 安全场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("STUDENT 用户绕过 PermissionAccess（非活动状态）返回 200")
        void studentUserAccessDenied() throws Exception {
            CurrentUserPrincipal student = new CurrentUserPrincipal() {
                @Override
                public String getId() {
                    return "stu-001";
                }

                @Override
                public UserType getUserType() {
                    return UserType.STUDENT;
                }

                @Override
                public String getUniversityId() {
                    return "uni1";
                }
            };
            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            student, null, Collections.emptyList()
                    )
            );

            when(recordService.getByChapterId("ch-001"))
                    .thenReturn(java.util.List.of());
            mockMvc.perform(get("/api/admin/learning/records/chapter/ch-001"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("XSS 注入在路径参数中返回 404（特殊字符导致 URL 不匹配）")
        void xssInPath() throws Exception {
            mockMvc.perform(get("/api/admin/learning/records/chapter/<script>alert('xss')</script>"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("SQL 注入在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(recordService.getByChapterId("' OR '1'='1"))
                    .thenThrow(new BusinessException(4000, "章节不存在"));

            mockMvc.perform(get("/api/admin/learning/records/chapter/' OR '1'='1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/learning/records/chapter/ch-001"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("POST 请求删除接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/learning/records/rec-001"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
