package com.rauio.smartdangjian.server.ai.pojo.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI提示词角色")
public enum PromptRoleEnum {
    @Schema(description = "系统角色")
    SYSTEM,
    @Schema(description = "开发者角色")
    DEVELOPER
}
