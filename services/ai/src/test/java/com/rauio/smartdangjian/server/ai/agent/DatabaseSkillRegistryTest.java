package com.rauio.smartdangjian.server.ai.agent;

import com.alibaba.cloud.ai.graph.skills.SkillMetadata;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiSkill;
import com.rauio.smartdangjian.server.ai.service.SkillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseSkillRegistryTest {

    @Mock
    private SkillService skillService;

    private DatabaseSkillRegistry registry;

    @BeforeEach
    void setUp() {
        AiSkill skill = AiSkill.builder()
                .id("skill-1")
                .name("test-skill")
                .description("测试技能描述")
                .content("这是技能内容")
                .enabled(true)
                .sort(1)
                .toolGroups(List.of("learning", "quiz"))
                .build();
        when(skillService.listEnabledSkills()).thenReturn(List.of(skill));
        registry = new DatabaseSkillRegistry(skillService);
    }

    @Test
    @DisplayName("构造函数调用 reload 加载启用的技能")
    void constructorLoadsSkills() {
        assertThat(registry.contains("test-skill")).isTrue();
        assertThat(registry.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("get 返回已有技能的元数据")
    void getExistingSkill() {
        Optional<SkillMetadata> result = registry.get("test-skill");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("test-skill");
        assertThat(result.get().getDescription()).isEqualTo("测试技能描述");
        assertThat(result.get().getSource()).isEqualTo("database");
    }

    @Test
    @DisplayName("get 不存在的技能返回 Optional.empty")
    void getNonExistingSkill() {
        Optional<SkillMetadata> result = registry.get("nonexistent");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listAll 返回所有技能元数据列表")
    void listAll() {
        assertThat(registry.listAll()).hasSize(1);
        assertThat(registry.listAll().get(0).getName()).isEqualTo("test-skill");
    }

    @Test
    @DisplayName("contains 对存在技能返回 true")
    void containsExisting() {
        assertThat(registry.contains("test-skill")).isTrue();
    }

    @Test
    @DisplayName("contains 对不存在技能返回 false")
    void containsNonExisting() {
        assertThat(registry.contains("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("readSkillContent 返回已有技能的完整内容")
    void readSkillContentExisting() {
        String content = registry.readSkillContent("test-skill");
        assertThat(content).contains("这是技能内容");
        assertThat(content).contains("name: \"test-skill\"");
        assertThat(content).contains("toolGroups: [learning, quiz]");
    }

    @Test
    @DisplayName("readSkillContent 不存在技能抛出 BusinessException")
    void readSkillContentNonExisting() {
        assertThatThrownBy(() -> registry.readSkillContent("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Skill not found");
    }

    @Test
    @DisplayName("getSkillLoadInstructions 返回包含数据库相关说明")
    void getSkillLoadInstructions() {
        String instructions = registry.getSkillLoadInstructions();
        assertThat(instructions).contains("数据库");
        assertThat(instructions).contains("read_skill");
    }

    @Test
    @DisplayName("getRegistryType 返回 Database")
    void getRegistryType() {
        assertThat(registry.getRegistryType()).isEqualTo("Database");
    }

    @Test
    @DisplayName("getSystemPromptTemplate 返回非空的模板")
    void getSystemPromptTemplate() {
        assertThat(registry.getSystemPromptTemplate()).isNotNull();
    }

    @Test
    @DisplayName("reload 方法重新加载技能并更新缓存")
    void reloadUpdatesCache() {
        AiSkill newSkill = AiSkill.builder()
                .id("skill-2")
                .name("new-skill")
                .description("新技能描述")
                .content("新技能内容")
                .build();
        when(skillService.listEnabledSkills()).thenReturn(List.of(newSkill));

        registry.reload();

        assertThat(registry.contains("test-skill")).isFalse();
        assertThat(registry.contains("new-skill")).isTrue();
        assertThat(registry.size()).isEqualTo(1);
    }
}
