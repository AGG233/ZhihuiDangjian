package com.rauio.smartdangjian.controller.factory;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiPrompts;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiSkill;
import com.rauio.smartdangjian.server.ai.pojo.enums.PromptRoleEnum;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptUpdateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillUpdateRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiPromptResponse;

/**
 * Static factory for AI test data — produces AiPrompts, AiSkill, request DTOs, and a JSON helper.
 */
public final class AiTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private AiTestDataFactory() {}

    // ── AiPromptCreateRequest ───────────────────────────────────────

    public static AiPromptCreateRequest createAiPromptCreateRequest() {
        AiPromptCreateRequest req = new AiPromptCreateRequest();
        req.setAgentType("CHAT");
        req.setName("通用回复规范");
        req.setContent("你是党务学习助手，回答需严谨、客观、简洁。");
        req.setRole("SYSTEM");
        req.setEnabled(true);
        req.setSort(10);
        return req;
    }

    public static AiPromptCreateRequest createAiPromptCreateRequest(String agentType) {
        AiPromptCreateRequest req = new AiPromptCreateRequest();
        req.setAgentType(agentType);
        req.setName("提示词-" + agentType);
        req.setContent("Content for " + agentType);
        req.setRole("SYSTEM");
        req.setEnabled(true);
        req.setSort(10);
        return req;
    }

    // ── AiPromptUpdateRequest ───────────────────────────────────────

    public static AiPromptUpdateRequest createAiPromptUpdateRequest() {
        AiPromptUpdateRequest req = new AiPromptUpdateRequest();
        req.setName("更新后的提示词");
        req.setContent("更新后的内容");
        req.setEnabled(false);
        req.setSort(20);
        return req;
    }

    // ── AiSkillCreateRequest ────────────────────────────────────────

    public static AiSkillCreateRequest createAiSkillCreateRequest() {
        AiSkillCreateRequest req = new AiSkillCreateRequest();
        req.setAgentType("CHAT");
        req.setName("通用技能");
        req.setDescription("通用对话技能描述");
        req.setContent("## 技能内容\\n通用技能说明");
        req.setEnabled(true);
        req.setSort(10);
        req.setToolGroups(List.of("general"));
        return req;
    }

    // ── AiSkillUpdateRequest ────────────────────────────────────────

    public static AiSkillUpdateRequest createAiSkillUpdateRequest() {
        AiSkillUpdateRequest req = new AiSkillUpdateRequest();
        req.setName("更新后的技能");
        req.setDescription("更新后的描述");
        req.setContent("更新后的内容");
        req.setEnabled(false);
        req.setSort(20);
        req.setToolGroups(List.of("advanced"));
        return req;
    }

    // ── AiPrompts entity ────────────────────────────────────────────

    public static AiPrompts createAiPrompts(String id) {
        return AiPrompts.builder()
                .id(id)
                .agentType("CHAT")
                .name("通用回复规范")
                .category("通用")
                .content("你是党务学习助手，回答需严谨、客观、简洁。")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(true)
                .sort(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── AiPromptResponse ───────────────────────────────────────────

    public static AiPromptResponse createAiPromptResponse(String id) {
        return AiPromptResponse.builder()
                .id(id)
                .agentType("CHAT")
                .name("通用回复规范")
                .category("通用")
                .content("你是党务学习助手，回答需严谨、客观、简洁。")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(true)
                .sort(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── AiSkill entity ──────────────────────────────────────────────

    public static AiSkill createAiSkill(String id) {
        return AiSkill.builder()
                .id(id)
                .agentType("CHAT")
                .name("通用技能")
                .description("通用对话技能描述")
                .content("## 技能内容\n通用技能说明")
                .enabled(true)
                .sort(10)
                .toolGroups(List.of("general"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── JSON helper ─────────────────────────────────────────────────

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}
