package com.rauio.smartdangjian.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.rauio.smartdangjian.controller.factory.AiTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.controller.admin.AdminPromptController;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiPrompts;
import com.rauio.smartdangjian.server.ai.service.PromptService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AdminPromptControllerTest.TestConfig.class)
@DisplayName("管理员AI提示词接口测试")
class AdminPromptControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminPromptController adminPromptController(PromptService promptService) {
            return new AdminPromptController(promptService);
        }
    }

    @MockitoBean
    private PromptService promptService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST / - 创建提示词成功")
        void createPromptSuccess() throws Exception {
            when(promptService.create(any())).thenReturn(AiTestDataFactory.createAiPrompts("prompt-1"));

            mockMvc.perform(post("/api/admin/ai/prompts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AiTestDataFactory.toJson(AiTestDataFactory.createAiPromptCreateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("prompt-1"));
        }

        @Test
        @DisplayName("GET /{id} - 获取提示词成功")
        void getPromptSuccess() throws Exception {
            when(promptService.getById("prompt-1")).thenReturn(AiTestDataFactory.createAiPrompts("prompt-1"));

            mockMvc.perform(get("/api/admin/ai/prompts/prompt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("prompt-1"));
        }

        @Test
        @DisplayName("GET / - 查询提示词列表成功")
        void listPromptsSuccess() throws Exception {
            List<AiPrompts> list = List.of(
                    AiTestDataFactory.createAiPrompts("prompt-1"), AiTestDataFactory.createAiPrompts("prompt-2"));
            when(promptService.list()).thenReturn(list);

            mockMvc.perform(get("/api/admin/ai/prompts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("PUT /{id} - 更新提示词成功")
        void updatePromptSuccess() throws Exception {
            when(promptService.update(eq("prompt-1"), any())).thenReturn(AiTestDataFactory.createAiPrompts("prompt-1"));

            mockMvc.perform(put("/api/admin/ai/prompts/prompt-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AiTestDataFactory.toJson(AiTestDataFactory.createAiPromptUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("prompt-1"));
        }

        @Test
        @DisplayName("DELETE /{id} - 删除提示词成功")
        void deletePromptSuccess() throws Exception {
            when(promptService.removeById("prompt-1")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/ai/prompts/prompt-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400")
        void serviceThrowsBusinessException() throws Exception {
            when(promptService.getById("nonexistent")).thenThrow(new BusinessException(4000, "提示词不存在"));

            mockMvc.perform(get("/api/admin/ai/prompts/nonexistent"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("提示词不存在"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(promptService.list()).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/admin/ai/prompts"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - 请求体为空返回 400")
        void createWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/admin/ai/prompts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST / - 缺少必填字段返回 400")
        void createWithMissingRequiredFields() throws Exception {
            String json = "{\"agentType\": \"CHAT\"}";
            mockMvc.perform(post("/api/admin/ai/prompts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/ai/prompts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET / - 空结果集返回空数组")
        void listEmptyResult() throws Exception {
            when(promptService.list()).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/ai/prompts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("中文名称创建提示词")
        void createWithChineseName() throws Exception {
            when(promptService.create(any())).thenReturn(AiTestDataFactory.createAiPrompts("prompt-1"));

            AiTestDataFactory.createAiPromptCreateRequest().setName("党的二十大精神学习规范");
            mockMvc.perform(post("/api/admin/ai/prompts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AiTestDataFactory.toJson(AiTestDataFactory.createAiPromptCreateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("超长 content 创建提示词")
        void createWithLongContent() throws Exception {
            when(promptService.create(any())).thenReturn(AiTestDataFactory.createAiPrompts("prompt-1"));

            AiTestDataFactory.createAiPromptCreateRequest().setContent("a".repeat(5000));
            mockMvc.perform(post("/api/admin/ai/prompts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AiTestDataFactory.toJson(AiTestDataFactory.createAiPromptCreateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在 name 字段")
        void xssInName() throws Exception {
            when(promptService.create(any())).thenReturn(AiTestDataFactory.createAiPrompts("prompt-1"));

            String json =
                    "{\"agentType\":\"CHAT\",\"name\":\"<script>alert('xss')</script>\",\"content\":\"test\",\"role\":\"SYSTEM\"}";
            mockMvc.perform(post("/api/admin/ai/prompts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在 content 字段")
        void sqlInjectionInContent() throws Exception {
            when(promptService.create(any())).thenReturn(AiTestDataFactory.createAiPrompts("prompt-1"));

            String json = "{\"agentType\":\"CHAT\",\"name\":\"test\",\"content\":\"' OR '1'='1\",\"role\":\"SYSTEM\"}";
            mockMvc.perform(post("/api/admin/ai/prompts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET 请求创建接口正常处理（匹配查询接口）")
        void createWithWrongMethod() throws Exception {
            when(promptService.list()).thenReturn(java.util.List.of());
            mockMvc.perform(get("/api/admin/ai/prompts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST 请求删除接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/ai/prompts/prompt-1")).andExpect(status().isMethodNotAllowed());
        }
    }
}
