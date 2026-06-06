package com.rauio.smartdangjian.server.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.mapper.AiSkillMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiSkill;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillUpdateRequest;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private AiSkillMapper mapper;

    @Spy
    @InjectMocks
    private SkillService skillService;

    @BeforeEach
    void resetSpy() {
        reset(skillService);
    }

    @Test
    @DisplayName("事务边界按方法声明：读方法只读，写方法显式回滚")
    void transactionalBoundariesAreMethodLevel() throws NoSuchMethodException {
        assertThat(SkillService.class.getAnnotation(Transactional.class)).isNull();
        assertReadOnlyTransaction("listEnabledSkills");
        assertWriteTransaction("create", AiSkillCreateRequest.class);
        assertWriteTransaction("update", String.class, AiSkillUpdateRequest.class);
    }

    private void assertReadOnlyTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = SkillService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isTrue();
    }

    private void assertWriteTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = SkillService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isFalse();
        assertThat(transactional.rollbackFor()).contains(Exception.class);
    }

    @Test
    @DisplayName("create 构建 AiSkill 并保存")
    void create() {
        AiSkillCreateRequest request = new AiSkillCreateRequest();
        request.setAgentType("CHAT");
        request.setName("test-skill");
        request.setDescription("测试技能");
        request.setContent("技能内容");
        request.setEnabled(true);
        request.setSort(1);
        request.setToolGroups(List.of("learning"));

        doReturn(true).when(skillService).save(any(AiSkill.class));

        AiSkill result = skillService.create(request);

        assertThat(result.getAgentType()).isEqualTo("CHAT");
        assertThat(result.getName()).isEqualTo("test-skill");
        assertThat(result.getDescription()).isEqualTo("测试技能");
        assertThat(result.getContent()).isEqualTo("技能内容");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getSort()).isEqualTo(1);
        assertThat(result.getToolGroups()).containsExactly("learning");
        assertThat(result.getId()).isNotNull();
        verify(skillService).save(any(AiSkill.class));
    }

    @Test
    @DisplayName("create 使用默认值：enabled=false, sort=0")
    void createWithDefaults() {
        AiSkillCreateRequest request = new AiSkillCreateRequest();
        request.setAgentType("CHAT");
        request.setName("test");
        request.setDescription("desc");
        request.setContent("content");
        request.setEnabled(null);
        request.setSort(null);

        doReturn(true).when(skillService).save(any(AiSkill.class));

        AiSkill result = skillService.create(request);

        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getSort()).isZero();
    }

    @Test
    @DisplayName("update 更新已有技能的所有字段")
    void update() {
        AiSkill existing = AiSkill.builder()
                .id("1")
                .agentType("CHAT")
                .name("old-name")
                .description("old-desc")
                .content("old-content")
                .enabled(false)
                .sort(0)
                .build();

        doReturn(existing).when(skillService).getById("skill-1");
        doReturn(true).when(skillService).updateById(any(AiSkill.class));

        AiSkillUpdateRequest request = new AiSkillUpdateRequest();
        request.setAgentType("QUIZ");
        request.setName("new-name");
        request.setDescription("new-desc");
        request.setContent("new-content");
        request.setEnabled(true);
        request.setSort(5);
        request.setToolGroups(List.of("quiz"));

        AiSkill result = skillService.update("skill-1", request);

        assertThat(result.getAgentType()).isEqualTo("QUIZ");
        assertThat(result.getName()).isEqualTo("new-name");
        assertThat(result.getDescription()).isEqualTo("new-desc");
        assertThat(result.getContent()).isEqualTo("new-content");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getSort()).isEqualTo(5);
        assertThat(result.getToolGroups()).containsExactly("quiz");
        verify(skillService).updateById(any(AiSkill.class));
    }

    @Test
    @DisplayName("update 当技能不存在时抛出 BusinessException")
    void updateThrowsWhenNotFound() {
        doReturn(null).when(skillService).getById("nonexistent");

        AiSkillUpdateRequest request = new AiSkillUpdateRequest();

        assertThatThrownBy(() -> skillService.update("nonexistent", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("技能不存在");
    }

    @Test
    @DisplayName("update 只更新非 null 字段")
    void updateOnlyNonNullFields() {
        AiSkill existing = AiSkill.builder()
                .id("1")
                .agentType("CHAT")
                .name("name")
                .description("desc")
                .content("content")
                .enabled(false)
                .sort(0)
                .build();

        doReturn(existing).when(skillService).getById("skill-1");
        doReturn(true).when(skillService).updateById(any(AiSkill.class));

        AiSkillUpdateRequest request = new AiSkillUpdateRequest();
        request.setName("new-name");

        AiSkill result = skillService.update("skill-1", request);

        assertThat(result.getAgentType()).isEqualTo("CHAT");
        assertThat(result.getName()).isEqualTo("new-name");
        assertThat(result.getDescription()).isEqualTo("desc");
        assertThat(result.getContent()).isEqualTo("content");
        verify(skillService).updateById(any(AiSkill.class));
    }

    @Test
    @DisplayName("update only updates non-null fields without name")
    void updateOnlyNonNullFieldsWithoutName() {
        AiSkill existing = AiSkill.builder()
                .id("1")
                .agentType("CHAT")
                .name("保留名称")
                .description("保留描述")
                .content("保留内容")
                .enabled(false)
                .sort(0)
                .build();

        doReturn(existing).when(skillService).getById("skill-1");
        doReturn(true).when(skillService).updateById(any(AiSkill.class));

        AiSkillUpdateRequest request = new AiSkillUpdateRequest();
        request.setContent("仅更新内容");
        request.setEnabled(true);

        AiSkill result = skillService.update("skill-1", request);

        assertThat(result.getName()).isEqualTo("保留名称");
        assertThat(result.getDescription()).isEqualTo("保留描述");
        assertThat(result.getContent()).isEqualTo("仅更新内容");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getSort()).isZero();
        verify(skillService).updateById(any(AiSkill.class));
    }

    @Test
    @DisplayName("listEnabledSkills returns enabled skills list")
    void listEnabledSkills() {
        AiSkill skill1 = AiSkill.builder().id("1").name("s1").enabled(true).build();
        AiSkill skill2 = AiSkill.builder().id("1").name("s2").enabled(true).build();
        doReturn(List.of(skill1, skill2)).when(skillService).list(any(LambdaQueryWrapper.class));

        List<AiSkill> result = skillService.listEnabledSkills();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("s1");
    }
}
