package com.rauio.smartdangjian.server.ai.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "AI提示词角色")
public enum PromptRoleEnum {
    @Schema(description = "系统角色")
    SYSTEM("system"),
    @Schema(description = "开发者角色")
    DEVELOPER("developer"),
    @Schema(description = "用户角色")
    USER("user");

    @EnumValue
    @JsonValue
    @Getter
    @Schema(hidden = true)
    private final String value;

    PromptRoleEnum(String value) {
        this.value = value;
    }
}
