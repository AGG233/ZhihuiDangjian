package com.rauio.smartdangjian.server.ai.pojo.response;

import java.time.LocalDateTime;

import com.rauio.smartdangjian.server.ai.pojo.enums.PromptRoleEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI系统提示词响应")
public class AiPromptResponse {

    @Schema(description = "提示词ID", example = "1")
    private String id;

    private String agentType;

    @Schema(description = "提示词名称", example = "通用回复规范")
    private String name;

    @Schema(description = "提示词类别", example = "通用")
    private String category;

    @Schema(description = "提示词内容", example = "你是党务学习助手，回答需严谨、客观、简洁。")
    private String content;

    @Schema(description = "提示词角色", example = "SYSTEM")
    private PromptRoleEnum role;

    private Boolean enabled;

    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
