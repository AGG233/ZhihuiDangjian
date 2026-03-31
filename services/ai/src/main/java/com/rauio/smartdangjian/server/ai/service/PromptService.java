package com.rauio.smartdangjian.server.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.ai.mapper.AiPromptsMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiPrompts;
import com.rauio.smartdangjian.server.ai.pojo.enums.PromptRoleEnum;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PromptService extends ServiceImpl<AiPromptsMapper, AiPrompts> {

    public AiPrompts create(AiPromptCreateRequest request) {
        AiPrompts prompt = AiPrompts.builder()
                .id(UUID.randomUUID().toString())
                .agentType(request.getAgentType().toUpperCase())
                .name(request.getName())
                .content(request.getContent())
                .role(PromptRoleEnum.valueOf(request.getRole().toUpperCase()))
                .enabled(Boolean.TRUE.equals(request.getEnabled()))
                .sort(request.getSort() == null ? 0 : request.getSort())
                .build();
        this.save(prompt);
        return prompt;
    }

    public AiPrompts update(String id, AiPromptUpdateRequest request) {
        AiPrompts prompt = this.getById(id);
        if (prompt == null) {
            throw new IllegalArgumentException("提示词不存在: " + id);
        }
        if (request.getAgentType() != null) {
            prompt.setAgentType(request.getAgentType().toUpperCase());
        }
        if (request.getName() != null) {
            prompt.setName(request.getName());
        }
        if (request.getContent() != null) {
            prompt.setContent(request.getContent());
        }
        if (request.getRole() != null) {
            prompt.setRole(PromptRoleEnum.valueOf(request.getRole().toUpperCase()));
        }
        if (request.getEnabled() != null) {
            prompt.setEnabled(request.getEnabled());
        }
        if (request.getSort() != null) {
            prompt.setSort(request.getSort());
        }
        this.updateById(prompt);
        return prompt;
    }

    public List<AiPrompts> listEnabledSystemPrompts(String agentType) {
        return this.list(new LambdaQueryWrapper<AiPrompts>()
                .eq(AiPrompts::getEnabled, true)
                .eq(AiPrompts::getRole, PromptRoleEnum.SYSTEM)
                .and(wrapper -> wrapper.eq(AiPrompts::getAgentType, "COMMON").or().eq(AiPrompts::getAgentType, agentType))
                .orderByAsc(AiPrompts::getSort, AiPrompts::getUpdatedAt));
    }

    public String buildSystemPrompt(String agentType) {
        List<AiPrompts> prompts = listEnabledSystemPrompts(agentType);
        if (prompts.isEmpty()) {
            return """
                    你是智慧党建平台AI助手。
                    回答要求：
                    1. 使用中文，表达准确、简洁、可执行。
                    2. 结论必须基于可获取信息，不得编造事实。
                    3. 结合用户学习、答题和会话记忆时要显式区分“已知信息”和“推断建议”。
                    """;
        }
        return prompts.stream()
                .map(AiPrompts::getContent)
                .filter(content -> content != null && !content.isBlank())
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("");
    }
}
