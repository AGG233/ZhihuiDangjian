package com.rauio.smartdangjian.server.ai.pojo.response;

import java.time.LocalDateTime;
import java.util.List;

import com.rauio.smartdangjian.server.ai.pojo.entity.AiSkill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "AI技能响应")
public record AiSkillResponse(
        @Schema(description = "技能ID") String id,
        @Schema(description = "智能体类型") String agentType,
        @Schema(description = "技能名称") String name,
        @Schema(description = "技能描述") String description,
        @Schema(description = "技能内容") String content,
        @Schema(description = "是否启用") Boolean enabled,
        @Schema(description = "排序") Integer sort,
        @Schema(description = "工具组") List<String> toolGroups,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {

    public static AiSkillResponse from(AiSkill skill) {
        if (skill == null) {
            return null;
        }
        return AiSkillResponse.builder()
                .id(skill.getId())
                .agentType(skill.getAgentType())
                .name(skill.getName())
                .description(skill.getDescription())
                .content(skill.getContent())
                .enabled(skill.getEnabled())
                .sort(skill.getSort())
                .toolGroups(skill.getToolGroups())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }
}
