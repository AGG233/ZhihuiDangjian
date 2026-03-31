package com.rauio.smartdangjian.server.ai.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI技能更新请求")
public class AiSkillUpdateRequest {

    private String agentType;

    private String name;

    private String description;

    private String content;

    private Boolean enabled;

    @Min(0)
    private Integer sort;

    private List<String> toolGroups;
}
