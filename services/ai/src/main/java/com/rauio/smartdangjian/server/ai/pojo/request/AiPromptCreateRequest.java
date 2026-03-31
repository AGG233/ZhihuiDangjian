package com.rauio.smartdangjian.server.ai.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "AI系统提示词创建请求")
@Data
public class AiPromptCreateRequest {

    @Schema(description = "Agent类型", example = "CHAT")
    @NotBlank(message = "agentType不能为空")
    private String agentType;

    @Schema(description = "提示词名称", example = "通用回复规范")
    @NotBlank(message = "name不能为空")
    private String name;

    @Schema(description = "提示词内容", example = "你是党务学习助手，回答需严谨、客观、简洁。")
    @NotBlank(message = "content不能为空")
    private String content;

    @Schema(description = "提示词角色", example = "SYSTEM")
    @NotBlank(message = "role不能为空")
    private String role;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序号(升序)", example = "10")
    @Min(value = 0, message = "sort不能小于0")
    private Integer sort;
}
