package com.rauio.smartdangjian.controller.user;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.LearningTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserChapterProgressDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserChapterProgressVO;
import com.rauio.smartdangjian.server.learning.controller.user.UserChapterProgressController;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = UserChapterProgressControllerTest.TestConfig.class)
@DisplayName("用户章节进度接口测试")
class UserChapterProgressControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserChapterProgressController userChapterProgressController(UserChapterProgressService progressService) {
            return new UserChapterProgressController(progressService);
        }
    }

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
            UserChapterProgressVO vo = LearningTestDataFactory.createChapterProgressVO("prog-001");
            when(progressService.get("prog-001")).thenReturn(vo);

            mockMvc.perform(get("/api/learning/progress/prog-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("prog-001"))
                    .andExpect(jsonPath("$.data.userId").value("user-001"))
                    .andExpect(jsonPath("$.data.chapterId").value("ch-001"));
        }

        @Test
        @DisplayName("GET /user/{userId} - 获取用户所有进度成功")
        void getByUserIdSuccess() throws Exception {
            UserChapterProgressVO vo = LearningTestDataFactory.createChapterProgressVO("prog-001");
            when(progressService.getByUserId("user-001")).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/learning/progress/user/user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].id").value("prog-001"));
        }

        @Test
        @DisplayName("GET /user/{userId}/chapter/{chapterId} - 获取用户章节进度成功")
        void getByUserIdAndChapterIdSuccess() throws Exception {
            UserChapterProgressVO vo = LearningTestDataFactory.createChapterProgressVO("prog-001");
            when(progressService.getByUserIdAndChapterId("user-001", "ch-001")).thenReturn(vo);

            mockMvc.perform(get("/api/learning/progress/user/user-001/chapter/ch-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("prog-001"));
        }

        @Test
        @DisplayName("POST / - 创建进度记录成功")
        void createSuccess() throws Exception {
            when(progressService.create(any(UserChapterProgressDto.class))).thenReturn(true);

            mockMvc.perform(post("/api/learning/progress/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT / - 更新进度记录成功")
        void updateSuccess() throws Exception {
            when(progressService.update(any(UserChapterProgressDto.class))).thenReturn(true);

            mockMvc.perform(put("/api/learning/progress/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressUpdateDto("prog-001"))))
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
            when(progressService.get("prog-001"))
                    .thenThrow(new BusinessException(4000, "进度记录不存在"));

            mockMvc.perform(get("/api/learning/progress/prog-001"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("进度记录不存在"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 RuntimeException 返回 500")
        void getThrowsRuntimeException() throws Exception {
            when(progressService.get("prog-001"))
                    .thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/learning/progress/prog-001"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - Service 抛出 BusinessException 返回 400")
        void createThrowsBusinessException() throws Exception {
            when(progressService.create(any(UserChapterProgressDto.class)))
                    .thenThrow(new BusinessException(4000, "该用户的章节进度记录已存在"));

            mockMvc.perform(post("/api/learning/progress/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressDto())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("该用户的章节进度记录已存在"));
        }

        @Test
        @DisplayName("POST / - Service 抛出 RuntimeException 返回 500")
        void createThrowsRuntimeException() throws Exception {
            when(progressService.create(any(UserChapterProgressDto.class)))
                    .thenThrow(new RuntimeException("创建进度记录失败"));

            mockMvc.perform(post("/api/learning/progress/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressDto())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - Service 返回 false 时 code 为 400")
        void createReturnsFalse() throws Exception {
            when(progressService.create(any(UserChapterProgressDto.class))).thenReturn(false);

            mockMvc.perform(post("/api/learning/progress/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("Operation failed"));
        }

        @Test
        @DisplayName("PUT / - Service 抛出 BusinessException 返回 400")
        void updateThrowsBusinessException() throws Exception {
            when(progressService.update(any(UserChapterProgressDto.class)))
                    .thenThrow(new BusinessException(4000, "进度记录不存在"));

            mockMvc.perform(put("/api/learning/progress/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressUpdateDto("nonexistent"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("进度记录不存在"));
        }

        @Test
        @DisplayName("PUT / - Service 返回 false 时 code 为 400")
        void updateReturnsFalse() throws Exception {
            when(progressService.update(any(UserChapterProgressDto.class))).thenReturn(false);

            mockMvc.perform(put("/api/learning/progress/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LearningTestDataFactory.toJson(LearningTestDataFactory.createChapterProgressUpdateDto("prog-001"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("Operation failed"));
        }

        @Test
        @DisplayName("POST / - 请求体为空返回 400")
        void createWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/learning/progress/")
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
            when(progressService.getByUserId("user-empty")).thenReturn(List.of());

            mockMvc.perform(get("/api/learning/progress/user/user-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /user/{userId} - 多进度记录返回")
        void getByUserIdMultipleRecords() throws Exception {
            List<UserChapterProgressVO> list = List.of(
                    LearningTestDataFactory.createChapterProgressVO("prog-001", "user-001", "ch-001"),
                    LearningTestDataFactory.createChapterProgressVO("prog-002", "user-001", "ch-002")
            );
            when(progressService.getByUserId("user-001")).thenReturn(list);

            mockMvc.perform(get("/api/learning/progress/user/user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("PUT / - Service 处理空请求体（缺失字段）")
        void updateWithPartialBody() throws Exception {
            when(progressService.update(any(UserChapterProgressDto.class))).thenReturn(true);

            mockMvc.perform(put("/api/learning/progress/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\":\"prog-001\"}"))
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
            when(progressService.get("<script>alert('xss')</script>"))
                    .thenThrow(new BusinessException(4000, "进度记录不存在"));

            mockMvc.perform(get(URI.create("/api/learning/progress/%3Cscript%3Ealert('xss')%3C%2Fscript%3E")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("SQL 注入在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(progressService.get("' OR '1'='1"))
                    .thenThrow(new BusinessException(4000, "进度记录不存在"));

            mockMvc.perform(get("/api/learning/progress/{id}", "' OR '1'='1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/learning/progress/prog-001"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("GET 请求创建接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/learning/progress/"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求更新接口返回 405")
        void updateWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/learning/progress/"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
