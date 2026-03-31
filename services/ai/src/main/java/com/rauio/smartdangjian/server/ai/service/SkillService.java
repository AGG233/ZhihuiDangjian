package com.rauio.smartdangjian.server.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.ai.mapper.AiSkillMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiSkill;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillService extends ServiceImpl<AiSkillMapper, AiSkill> {

    public AiSkill create(AiSkillCreateRequest request) {
        AiSkill skill = AiSkill.builder()
                .id(UUID.randomUUID().toString())
                .agentType(request.getAgentType().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .content(request.getContent())
                .enabled(Boolean.TRUE.equals(request.getEnabled()))
                .sort(request.getSort() == null ? 0 : request.getSort())
                .toolGroups(request.getToolGroups())
                .build();
        this.save(skill);
        return skill;
    }

    public AiSkill update(String id, AiSkillUpdateRequest request) {
        AiSkill skill = this.getById(id);
        if (skill == null) {
            throw new IllegalArgumentException("技能不存在: " + id);
        }
        if (request.getAgentType() != null) {
            skill.setAgentType(request.getAgentType().toUpperCase());
        }
        if (request.getName() != null) {
            skill.setName(request.getName());
        }
        if (request.getDescription() != null) {
            skill.setDescription(request.getDescription());
        }
        if (request.getContent() != null) {
            skill.setContent(request.getContent());
        }
        if (request.getEnabled() != null) {
            skill.setEnabled(request.getEnabled());
        }
        if (request.getSort() != null) {
            skill.setSort(request.getSort());
        }
        if (request.getToolGroups() != null) {
            skill.setToolGroups(request.getToolGroups());
        }
        this.updateById(skill);
        return skill;
    }

    public List<AiSkill> listEnabledSkills() {
        return this.list(new LambdaQueryWrapper<AiSkill>()
                .eq(AiSkill::getEnabled, true)
                .orderByAsc(AiSkill::getSort, AiSkill::getUpdatedAt));
    }
}
