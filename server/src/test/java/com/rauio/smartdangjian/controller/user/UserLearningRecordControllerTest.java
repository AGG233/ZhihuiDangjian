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
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.LearningTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.learning.constants.LearningErrorConstants;
import com.rauio.smartdangjian.server.learning.controller.user.UserLearningRecordController;
import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserLearningRecordControllerTest.TestConfig.class)
@DisplayName("用户学习记录接口测试")
class UserLearningRecordControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserLearningRecordController userLearningRecordController(UserLearningRecordService recordService) {
            return new UserLearningRecordController(recordService);
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
        @DisplayName("GET /{id} - 获取学习记录成功")
        void getSuccess() throws Exception {
            UserLearningRecordResponse vo = LearningTestDataFactory.createLearningRecordVO(1L);
            when(recordService.get(1L)).thenReturn(vo);

            mockMvc.perform(get("/api/learning/records/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.userId").value("1"))
                    .andExpect(jsonPath("$.data.chapterId").value("1"));
        }

        @Test
        @DisplayName("GET /user/{userId} - 获取用户所有学习记录成功")
        void getByUserIdSuccess() throws Exception {
            UserLearningRecordResponse vo = LearningTestDataFactory.createLearningRecordVO(1L);
            when(recordService.getByUserId(1L)).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/learning/records/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].id").value("1"));
        }

        @Test
        @DisplayName("GET /user/{userId}/chapter/{chapterId} - 获取用户章节学习记录成功")
        void getByUserIdAndChapterIdSuccess() throws Exception {
            UserLearningRecordResponse vo = LearningTestDataFactory.createLearningRecordVO(1L);
            when(recordService.getByUserIdAndChapterId(1L, 1L)).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/learning/records/users/1/chapters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].id").value("1"));
        }

        @Test
        @DisplayName("POST / - 创建学习记录成功")
        void createSuccess() throws Exception {
            when(recordService.create(any(UserLearningRecordRequest.class))).thenReturn(true);

            mockMvc.perform(post("/api/learning/records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createLearningRecordDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT / - 更新学习记录成功")
        void updateSuccess() throws Exception {
            when(recordService.update(any(UserLearningRecordRequest.class))).thenReturn(true);

            mockMvc.perform(put("/api/learning/records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(
                                    LearningTestDataFactory.createLearningRecordUpdateDto(1L))))
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
            when(recordService.get(1L))
                    .thenThrow(new BusinessException(LearningErrorConstants.RECORD_NOT_FOUND, "学习记录不存在"));

            mockMvc.perform(get("/api/learning/records/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("学习记录不存在"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 RuntimeException 返回 500")
        void getThrowsRuntimeException() throws Exception {
            when(recordService.get(1L)).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/learning/records/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - Service 抛出 BusinessException 返回 400")
        void createThrowsBusinessException() throws Exception {
            when(recordService.create(any(UserLearningRecordRequest.class)))
                    .thenThrow(new BusinessException(LearningErrorConstants.RECORD_CREATE_FAILED, "创建学习记录失败"));

            mockMvc.perform(post("/api/learning/records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createLearningRecordDto())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4002"))
                    .andExpect(jsonPath("$.message").value("创建学习记录失败"));
        }

        @Test
        @DisplayName("POST / - Service 抛出 RuntimeException 返回 500")
        void createThrowsRuntimeException() throws Exception {
            when(recordService.create(any(UserLearningRecordRequest.class)))
                    .thenThrow(new RuntimeException("创建学习记录异常"));

            mockMvc.perform(post("/api/learning/records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createLearningRecordDto())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - Service 返回 false 时 code 为 400")
        void createReturnsFalse() throws Exception {
            when(recordService.create(any(UserLearningRecordRequest.class))).thenReturn(false);

            mockMvc.perform(post("/api/learning/records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createLearningRecordDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("PUT / - Service 抛出 BusinessException 返回 400")
        void updateThrowsBusinessException() throws Exception {
            when(recordService.update(any(UserLearningRecordRequest.class)))
                    .thenThrow(new BusinessException(LearningErrorConstants.RECORD_NOT_FOUND, "学习记录不存在"));

            mockMvc.perform(put("/api/learning/records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(
                                    LearningTestDataFactory.createLearningRecordUpdateDto(9999L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("学习记录不存在"));
        }

        @Test
        @DisplayName("PUT / - Service 返回 false 时 code 为 400")
        void updateReturnsFalse() throws Exception {
            when(recordService.update(any(UserLearningRecordRequest.class))).thenReturn(false);

            mockMvc.perform(put("/api/learning/records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(
                                    LearningTestDataFactory.createLearningRecordUpdateDto(1L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("POST / - 请求体为空返回 400")
        void createWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/learning/records")
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
        @DisplayName("GET /user/{userId} - 空结果集返回空列表")
        void getByUserIdEmptyResult() throws Exception {
            when(recordService.getByUserId(9999L)).thenReturn(List.of());

            mockMvc.perform(get("/api/learning/records/users/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /user/{userId} - 多学习记录返回")
        void getByUserIdMultipleRecords() throws Exception {
            List<UserLearningRecordResponse> list = List.of(
                    LearningTestDataFactory.createLearningRecordVO(1L, 1L, 1L),
                    LearningTestDataFactory.createLearningRecordVO(2L, 1L, 2L));
            when(recordService.getByUserId(1L)).thenReturn(list);

            mockMvc.perform(get("/api/learning/records/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("GET /user/{userId}/chapter/{chapterId} - 空结果集返回空列表")
        void getByUserIdAndChapterIdEmptyResult() throws Exception {
            when(recordService.getByUserIdAndChapterId(9999L, 9999L)).thenReturn(List.of());

            mockMvc.perform(get("/api/learning/records/users/9999/chapters/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("PUT / - Service 处理部分字段更新")
        void updateWithPartialBody() throws Exception {
            when(recordService.update(any(UserLearningRecordRequest.class))).thenReturn(true);

            mockMvc.perform(put("/api/learning/records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\":1,\"duration\":7200}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
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
            when(recordService.get(anyLong()))
                    .thenThrow(new BusinessException(LearningErrorConstants.RECORD_NOT_FOUND, "学习记录不存在"));

            mockMvc.perform(get(URI.create("/api/learning/records/%3Cscript%3Ealert('xss')%3E")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("SQL 注入在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(recordService.get(anyLong()))
                    .thenThrow(new BusinessException(LearningErrorConstants.RECORD_NOT_FOUND, "学习记录不存在"));

            mockMvc.perform(get("/api/learning/records/{id}", "' OR '1'='1")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/learning/records/1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("GET 请求创建接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/learning/records")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求更新接口返回 405")
        void updateWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/learning/records")).andExpect(status().isMethodNotAllowed());
        }
    }
}
