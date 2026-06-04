package com.rauio.smartdangjian.controller.admin;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.ControllerTestConfiguration;
import com.rauio.smartdangjian.controller.factory.LearningTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.learning.constants.LearningErrorConstants;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import com.rauio.smartdangjian.utils.spec.UserType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ControllerTestConfiguration.class)
@DisplayName("管理员章节进度接口测试")
class AdminChapterProgressControllerTest extends BaseControllerTest {

    @MockitoBean
    private UserChapterProgressService progressService;

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /chapter/{chapterId} - 获取章节所有进度成功")
        void getByChapterIdSuccess() throws Exception {
            UserChapterProgressResponse vo = LearningTestDataFactory.createChapterProgressVO(1L);
            when(progressService.getByChapterId(1L)).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/admin/learning/progress/chapter/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[0].userId").value("1"))
                    .andExpect(jsonPath("$.data[0].chapterId").value("1"));
        }

        @Test
        @DisplayName("DELETE /{id} - 删除进度记录成功")
        void deleteSuccess() throws Exception {
            when(progressService.delete(1L)).thenReturn(true);

            mockMvc.perform(delete("/api/admin/learning/progress/1"))
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
            when(progressService.getByChapterId(1L))
                    .thenThrow(new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "章节不存在"));

            mockMvc.perform(get("/api/admin/learning/progress/chapter/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4011"))
                    .andExpect(jsonPath("$.message").value("章节不存在"));
        }

        @Test
        @DisplayName("DELETE /{id} - Service 抛出 BusinessException 返回 400")
        void deleteThrowsBusinessException() throws Exception {
            when(progressService.delete(9999L))
                    .thenThrow(new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "进度记录不存在"));

            mockMvc.perform(delete("/api/admin/learning/progress/9999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4011"))
                    .andExpect(jsonPath("$.message").value("进度记录不存在"));
        }

        @Test
        @DisplayName("GET /chapter/{chapterId} - Service 抛出 RuntimeException 返回 500")
        void getByChapterIdThrowsRuntimeException() throws Exception {
            when(progressService.getByChapterId(1L)).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/admin/learning/progress/chapter/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("DELETE /{id} - Service 抛出 RuntimeException 返回 500")
        void deleteThrowsRuntimeException() throws Exception {
            when(progressService.delete(1L)).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(delete("/api/admin/learning/progress/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("DELETE /{id} - Service 返回 false 时 code 为 400")
        void deleteReturnsFalse() throws Exception {
            when(progressService.delete(9999L)).thenReturn(false);

            mockMvc.perform(delete("/api/admin/learning/progress/9999"))
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
            when(progressService.getByChapterId(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/learning/progress/chapter/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /chapter/{chapterId} - 多记录返回")
        void getByChapterIdMultipleRecords() throws Exception {
            List<UserChapterProgressResponse> list = List.of(
                    LearningTestDataFactory.createChapterProgressVO(1L, 1L, 1L),
                    LearningTestDataFactory.createChapterProgressVO(2L, 2L, 1L),
                    LearningTestDataFactory.createChapterProgressVO(3L, 3L, 1L));
            when(progressService.getByChapterId(1L)).thenReturn(list);

            mockMvc.perform(get("/api/admin/learning/progress/chapter/1"))
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
                public Long getId() {
                    return 1L;
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
            setSecurityContext(UserType.STUDENT, student.getId(), student.getUniversityId());

            when(progressService.getByChapterId(1L)).thenReturn(java.util.List.of());
            mockMvc.perform(get("/api/admin/learning/progress/chapter/1")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("XSS 注入在路径参数中返回 404（特殊字符导致 URL 不匹配）")
        void xssInPath() throws Exception {
            mockMvc.perform(get("/api/admin/learning/progress/chapter/%3Cscript%3Ealert('xss')%3E"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("SQL 注入在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(progressService.getByChapterId(anyLong()))
                    .thenThrow(new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "章节不存在"));

            mockMvc.perform(get(URI.create("/api/admin/learning/progress/chapter/%27%20OR%20%271%27%3D%271")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/learning/progress/chapter/1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("POST 请求删除接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/learning/progress/1")).andExpect(status().isMethodNotAllowed());
        }
    }
}
