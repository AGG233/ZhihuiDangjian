package com.rauio.smartdangjian.server.ai.pojo.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI技能创建请求")
public class AiSkillCreateRequest {

    @NotBlank
    private String agentType;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String content;

    private Boolean enabled;

    @Min(0)
    private Integer sort;

    private List<String> toolGroups;
}
