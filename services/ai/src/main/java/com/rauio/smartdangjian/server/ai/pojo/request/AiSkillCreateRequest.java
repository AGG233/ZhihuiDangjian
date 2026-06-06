package com.rauio.smartdangjian.server.ai.pojo.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI技能创建请求")
public class AiSkillCreateRequest {

    @NotBlank(message = "agentType不能为空")
    private String agentType;

    @NotBlank(message = "name不能为空")
    private String name;

    @NotBlank(message = "description不能为空")
    private String description;

    @NotBlank(message = "content不能为空")
    private String content;

    private Boolean enabled;

    @Min(value = 0, message = "sort不能小于0")
    private Integer sort;

    private List<String> toolGroups;
}
