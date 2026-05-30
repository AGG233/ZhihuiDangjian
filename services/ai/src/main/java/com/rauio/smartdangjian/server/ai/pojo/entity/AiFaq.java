package com.rauio.smartdangjian.server.ai.pojo.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "ai_faq", autoResultMap = true)
@Schema(description = "AI FAQ快速回复规则")
public class AiFaq extends Model<AiFaq> {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "FAQ ID")
    private Long id;

    @Schema(description = "触发关键词（逗号分隔）", example = "入党流程,入党条件")
    private String keywords;

    @Schema(description = "问题摘要（可读）", example = "入党需要什么条件？")
    private String question;

    @Schema(description = "预定义答案", example = "根据党章规定...")
    private String answer;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序序号（越小优先级越高）", example = "0")
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
