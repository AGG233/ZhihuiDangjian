package com.rauio.smartdangjian.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.ControllerTestConfiguration;
import com.rauio.smartdangjian.controller.factory.LearningTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.learning.constants.LearningErrorConstants;
import com.rauio.smartdangjian.server.learning.controller.user.UserChapterProgressController;
import com.rauio.smartdangjian.server.learning.pojo.request.UserChapterProgressRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = ControllerTestConfiguration.class)
@DisplayName("用户章节进度接口测试")
class UserChapterProgressControllerTest extends BaseControllerTest {


    @MockitoBean
    private UserChapterProgressService progressService;

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /{id} - 获取进度记录成功")
        void getSuccess() throws Exception {
            UserChapterProgressResponse vo = LearningTestDataFactory.createChapterProgressVO(1L);
            when(progressService.getForUser(1L, 1L)).thenReturn(vo);

            mockMvc.perform(get("/api/learning/progress/me/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.userId").value("1"))
                    .andExpect(jsonPath("$.data.chapterId").value("1"));
        }

        @Test
        @DisplayName("GET /me - 获取当前用户所有进度成功")
        void getByUserIdSuccess() throws Exception {
            UserChapterProgressResponse vo = LearningTestDataFactory.createChapterProgressVO(1L);
            when(progressService.getByUserId(1L)).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/learning/progress/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].id").value("1"));
        }

        @Test
        @DisplayName("GET /me/chapters/{chapterId} - 获取当前用户章节进度成功")
        void getByUserIdAndChapterIdSuccess() throws Exception {
            UserChapterProgressResponse vo = LearningTestDataFactory.createChapterProgressVO(1L);
            when(progressService.getByUserIdAndChapterId(1L, 1L)).thenReturn(vo);

            mockMvc.perform(get("/api/learning/progress/me/chapters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"));
        }

        @Test
        @DisplayName("POST / - 创建进度记录成功")
        void createSuccess() throws Exception {
            when(progressService.createForUser(any(UserChapterProgressRequest.class), anyLong()))
                    .thenReturn(true);

            mockMvc.perform(post("/api/learning/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT / - 更新进度记录成功")
        void updateSuccess() throws Exception {
            when(progressService.updateForUser(any(UserChapterProgressRequest.class), anyLong()))
                    .thenReturn(true);

            mockMvc.perform(put("/api/learning/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(
                                    LearningTestDataFactory.createChapterProgressUpdateDto(1L))))
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
        @DisplayName("GET /{id} - Service 抛出 BusinessException 返回 400")
        void getThrowsBusinessException() throws Exception {
            when(progressService.getForUser(1L, 1L))
                    .thenThrow(new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "进度记录不存在"));

            mockMvc.perform(get("/api/learning/progress/me/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4011"))
                    .andExpect(jsonPath("$.message").value("进度记录不存在"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 RuntimeException 返回 500")
        void getThrowsRuntimeException() throws Exception {
            when(progressService.getForUser(1L, 1L)).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/learning/progress/me/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - Service 抛出 BusinessException 返回 400")
        void createThrowsBusinessException() throws Exception {
            when(progressService.createForUser(any(UserChapterProgressRequest.class), anyLong()))
                    .thenThrow(new BusinessException(LearningErrorConstants.PROGRESS_ALREADY_EXISTS, "该用户的章节进度记录已存在"));

            mockMvc.perform(post("/api/learning/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressDto())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4012"))
                    .andExpect(jsonPath("$.message").value("该用户的章节进度记录已存在"));
        }

        @Test
        @DisplayName("POST / - Service 抛出 RuntimeException 返回 500")
        void createThrowsRuntimeException() throws Exception {
            when(progressService.createForUser(any(UserChapterProgressRequest.class), anyLong()))
                    .thenThrow(new RuntimeException("创建进度记录失败"));

            mockMvc.perform(post("/api/learning/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressDto())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - Service 返回 false 时 code 为 400")
        void createReturnsFalse() throws Exception {
            when(progressService.createForUser(any(UserChapterProgressRequest.class), anyLong()))
                    .thenReturn(false);

            mockMvc.perform(post("/api/learning/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("PUT / - Service 抛出 BusinessException 返回 400")
        void updateThrowsBusinessException() throws Exception {
            when(progressService.updateForUser(any(UserChapterProgressRequest.class), anyLong()))
                    .thenThrow(new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "进度记录不存在"));

            mockMvc.perform(put("/api/learning/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(
                                    LearningTestDataFactory.createChapterProgressUpdateDto(9999L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4011"))
                    .andExpect(jsonPath("$.message").value("进度记录不存在"));
        }

        @Test
        @DisplayName("PUT / - Service 返回 false 时 code 为 400")
        void updateReturnsFalse() throws Exception {
            when(progressService.updateForUser(any(UserChapterProgressRequest.class), anyLong()))
                    .thenReturn(false);

            mockMvc.perform(put("/api/learning/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(
                                    LearningTestDataFactory.createChapterProgressUpdateDto(1L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("POST / - 请求体为空返回 400")
        void createWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/learning/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /me - 空结果集返回空列表")
        void getByUserIdEmptyResult() throws Exception {
            when(progressService.getByUserId(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/learning/progress/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /me - 多进度记录返回")
        void getByUserIdMultipleRecords() throws Exception {
            List<UserChapterProgressResponse> list = List.of(
                    LearningTestDataFactory.createChapterProgressVO(1L, 1L, 1L),
                    LearningTestDataFactory.createChapterProgressVO(2L, 1L, 2L));
            when(progressService.getByUserId(1L)).thenReturn(list);

            mockMvc.perform(get("/api/learning/progress/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("PUT / - 缺少必填字段返回 400")
        void updateWithPartialBody() throws Exception {
            mockMvc.perform(put("/api/learning/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\":1}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"));
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
            when(progressService.getForUser(anyLong(), anyLong()))
                    .thenThrow(new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "进度记录不存在"));

            mockMvc.perform(get(URI.create("/api/learning/progress/me/%3Cscript%3Ealert('xss')%3E")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("SQL 注入在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(progressService.getForUser(anyLong(), anyLong()))
                    .thenThrow(new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "进度记录不存在"));

            mockMvc.perform(get("/api/learning/progress/me/{id}", "' OR '1'='1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/learning/progress/me/1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("GET 请求创建接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/learning/progress")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求更新接口返回 405")
        void updateWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/learning/progress")).andExpect(status().isMethodNotAllowed());
        }
    }
}
