package com.rauio.smartdangjian.server.ai.agent;

import com.alibaba.cloud.ai.graph.skills.SkillMetadata;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiSkill;
import com.rauio.smartdangjian.server.ai.service.SkillService;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseSkillRegistry implements SkillRegistry {

    private static final String SKILL_SYSTEM_PROMPT = """
            ## Skills System

            你可以按需使用数据库中的技能库。每个技能都包含用途说明和完整执行方法。

            ### Available Skills
            {skills_list}

            ### How to Use Skills
            1. 当用户请求和某个技能描述匹配时，优先考虑调用该技能。
            2. 先通过 `read_skill` 读取完整技能说明，再决定是否执行。
            3. 只有在技能和当前任务强相关时才读取，避免无关展开。
            4. 技能内容来自数据库，允许动态更新，以当前读取结果为准。

            ### Skill Load Instructions
            {skills_load_instructions}
            """;

    private final SkillService skillService;
    private final Map<String, SkillMetadata> cache = new ConcurrentHashMap<>();
    private final SystemPromptTemplate systemPromptTemplate = SystemPromptTemplate.builder()
            .template(SKILL_SYSTEM_PROMPT)
            .build();

    public DatabaseSkillRegistry(SkillService skillService) {
        this.skillService = skillService;
        reload();
    }

    @Override
    public Optional<SkillMetadata> get(String name) {
        return Optional.ofNullable(cache.get(name));
    }

    @Override
    public List<SkillMetadata> listAll() {
        return cache.values().stream().toList();
    }

    @Override
    public boolean contains(String name) {
        return cache.containsKey(name);
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public void reload() {
        cache.clear();
        for (AiSkill skill : skillService.listEnabledSkills()) {
            cache.put(skill.getName(), SkillMetadata.builder()
                    .name(skill.getName())
                    .description(skill.getDescription())
                    .skillPath("db://ai_skill/" + skill.getId())
                    .source("database")
                    .fullContent(skill.renderSkillMarkdown())
                    .build());
        }
    }

    @Override
    public String readSkillContent(String name) {
        SkillMetadata metadata = cache.get(name);
        if (metadata == null) {
            throw new IllegalStateException("Skill not found: " + name);
        }
        return metadata.getFullContent();
    }

    @Override
    public String getSkillLoadInstructions() {
        return """
                - 技能存储介质：数据库
                - 读取方式：必须通过 `read_skill`
                - 生效规则：仅启用状态的技能会注入到当前Agent上下文
                - 更新规则：每次Agent执行前都会重新加载技能
                """;
    }

    @Override
    public String getRegistryType() {
        return "Database";
    }

    @Override
    public SystemPromptTemplate getSystemPromptTemplate() {
        return systemPromptTemplate;
    }
}
