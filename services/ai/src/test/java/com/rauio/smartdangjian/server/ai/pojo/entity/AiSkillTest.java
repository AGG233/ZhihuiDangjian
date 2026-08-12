package com.rauio.smartdangjian.server.ai.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiSkillTest {

    @Test
    @DisplayName("基本 Markdown 渲染")
    void basicRendering() {
        AiSkill skill = AiSkill.builder()
                .name("test-skill")
                .description("测试技能")
                .content("这是技能内容")
                .build();

        String result = skill.renderSkillMarkdown();

        assertThat(result).contains("name: \"test-skill\"");
        assertThat(result).contains("description: \"测试技能\"");
        assertThat(result).contains("这是技能内容");
    }

    @Test
    @DisplayName("YAML 特殊字符被转义")
    void yamlSpecialCharsEscaped() {
        AiSkill skill = AiSkill.builder()
                .name("skill: test")
                .description("描述包含 \"引号\" 和 # 号")
                .content("content")
                .build();

        String result = skill.renderSkillMarkdown();

        assertThat(result).contains("name: \"skill: test\"");
        assertThat(result).contains("\\\"引号\\\"");
    }

    @Test
    @DisplayName("null name 和 description 安全处理")
    void nullNameAndDescription() {
        AiSkill skill =
                AiSkill.builder().name(null).description(null).content(null).build();

        String result = skill.renderSkillMarkdown();

        assertThat(result).contains("name: \"\"");
        assertThat(result).contains("description: \"\"");
    }

    @Test
    @DisplayName("toolGroups 渲染到 Markdown")
    void toolGroupsRendered() {
        AiSkill skill = AiSkill.builder()
                .name("skill-with-tools")
                .description("带工具组的技能")
                .content("content")
                .toolGroups(List.of("learning", "quiz"))
                .build();

        String result = skill.renderSkillMarkdown();

        assertThat(result).contains("toolGroups: [learning, quiz]");
    }

    @Test
    @DisplayName("空 toolGroups 不渲染")
    void emptyToolGroupsNotRendered() {
        AiSkill skill = AiSkill.builder()
                .name("skill-no-tools")
                .description("无工具组")
                .content("content")
                .toolGroups(List.of())
                .build();

        String result = skill.renderSkillMarkdown();

        assertThat(result).doesNotContain("toolGroups");
    }
}
