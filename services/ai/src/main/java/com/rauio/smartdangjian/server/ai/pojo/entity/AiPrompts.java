package com.rauio.smartdangjian.server.ai.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.server.ai.pojo.enums.PromptRoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "ai_prompts", autoResultMap = true)
@Schema(description = "AI系统提示词")
public class AiPrompts {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "提示词ID", example = "1")
    private String id;

    private String agentType;

    @Schema(description = "提示词名称", example = "通用回复规范")
    private String name;

    @Schema(description = "提示词内容", example = "你是党务学习助手，回答需严谨、客观、简洁。")
    private String content;

    @Schema(description = "提示词角色", example = "SYSTEM")
    private PromptRoleEnum role;

    private Boolean enabled;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
