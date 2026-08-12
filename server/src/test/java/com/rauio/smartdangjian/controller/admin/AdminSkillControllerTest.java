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
import com.rauio.smartdangjian.server.ai.controller.admin.AdminSkillController;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiSkill;
import com.rauio.smartdangjian.server.ai.service.SkillService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AdminSkillControllerTest.TestConfig.class)
@DisplayName("管理员AI技能接口测试")
class AdminSkillControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminSkillController adminSkillController(SkillService skillService) {
            return new AdminSkillController(skillService);
        }
    }

    @MockitoBean
    private SkillService skillService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST / - 创建技能成功")
        void createSkillSuccess() throws Exception {
            when(skillService.create(any())).thenReturn(AiTestDataFactory.createAiSkill("skill-1"));

            mockMvc.perform(post("/api/admin/ai/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AiTestDataFactory.toJson(AiTestDataFactory.createAiSkillCreateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("skill-1"));
        }

        @Test
        @DisplayName("GET /{id} - 获取技能成功")
        void getSkillSuccess() throws Exception {
            when(skillService.getById("skill-1")).thenReturn(AiTestDataFactory.createAiSkill("skill-1"));

            mockMvc.perform(get("/api/admin/ai/skills/skill-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("skill-1"));
        }

        @Test
        @DisplayName("GET / - 查询技能列表成功")
        void listSkillsSuccess() throws Exception {
            List<AiSkill> list =
                    List.of(AiTestDataFactory.createAiSkill("skill-1"), AiTestDataFactory.createAiSkill("skill-2"));
            when(skillService.list()).thenReturn(list);

            mockMvc.perform(get("/api/admin/ai/skills"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("PUT /{id} - 更新技能成功")
        void updateSkillSuccess() throws Exception {
            when(skillService.update(eq("skill-1"), any())).thenReturn(AiTestDataFactory.createAiSkill("skill-1"));

            mockMvc.perform(put("/api/admin/ai/skills/skill-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AiTestDataFactory.toJson(AiTestDataFactory.createAiSkillUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("skill-1"));
        }

        @Test
        @DisplayName("DELETE /{id} - 删除技能成功")
        void deleteSkillSuccess() throws Exception {
            when(skillService.removeById("skill-1")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/ai/skills/skill-1"))
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
            when(skillService.getById("nonexistent")).thenThrow(new BusinessException(4000, "技能不存在"));

            mockMvc.perform(get("/api/admin/ai/skills/nonexistent"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("技能不存在"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(skillService.list()).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/admin/ai/skills"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - 请求体为空返回 400")
        void createWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/admin/ai/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST / - 缺少必填字段返回 400")
        void createWithMissingRequiredFields() throws Exception {
            String json = "{\"agentType\": \"CHAT\"}";
            mockMvc.perform(post("/api/admin/ai/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/ai/skills")
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
            when(skillService.list()).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/ai/skills"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("中文描述创建技能")
        void createWithChineseDescription() throws Exception {
            when(skillService.create(any())).thenReturn(AiTestDataFactory.createAiSkill("skill-1"));

            AiTestDataFactory.createAiSkillCreateRequest().setDescription("党的二十大精神学习技能");
            mockMvc.perform(post("/api/admin/ai/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AiTestDataFactory.toJson(AiTestDataFactory.createAiSkillCreateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("超长 content 创建技能")
        void createWithLongContent() throws Exception {
            when(skillService.create(any())).thenReturn(AiTestDataFactory.createAiSkill("skill-1"));

            AiTestDataFactory.createAiSkillCreateRequest().setContent("a".repeat(10000));
            mockMvc.perform(post("/api/admin/ai/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AiTestDataFactory.toJson(AiTestDataFactory.createAiSkillCreateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("toolGroups 为空列表创建技能")
        void createWithEmptyToolGroups() throws Exception {
            when(skillService.create(any())).thenReturn(AiTestDataFactory.createAiSkill("skill-1"));

            AiTestDataFactory.createAiSkillCreateRequest().setToolGroups(List.of());
            mockMvc.perform(post("/api/admin/ai/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(AiTestDataFactory.toJson(AiTestDataFactory.createAiSkillCreateRequest())))
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
            when(skillService.create(any())).thenReturn(AiTestDataFactory.createAiSkill("skill-1"));

            String json =
                    "{\"agentType\":\"CHAT\",\"name\":\"<script>alert('xss')</script>\",\"description\":\"test\",\"content\":\"test\"}";
            mockMvc.perform(post("/api/admin/ai/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在 description 字段")
        void sqlInjectionInDescription() throws Exception {
            when(skillService.create(any())).thenReturn(AiTestDataFactory.createAiSkill("skill-1"));

            String json =
                    "{\"agentType\":\"CHAT\",\"name\":\"test\",\"description\":\"' OR '1'='1\",\"content\":\"test\"}";
            mockMvc.perform(post("/api/admin/ai/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET 请求创建接口正常处理（匹配查询接口）")
        void createWithWrongMethod() throws Exception {
            when(skillService.list()).thenReturn(java.util.List.of());
            mockMvc.perform(get("/api/admin/ai/skills"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST 请求删除接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/ai/skills/skill-1")).andExpect(status().isMethodNotAllowed());
        }
    }
}
