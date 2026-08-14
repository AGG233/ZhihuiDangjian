package com.rauio.smartdangjian.server.quiz.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * SCORM 学习注册与成绩实体。
 *
 * <p>对应 scorm_registration 表，记录用户在某 SCO（可共享内容对象）上的学习状态与成绩，
 * 字段对应 cmi.core.lesson_status / score.* / session_time / total_time 数据模型。
 */
@Data
@Builder
@TableName("scorm_registration")
@Schema(description = "SCORM学习注册与成绩")
public class ScormRegistration {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "注册记录ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("package_id")
    @Schema(description = "学习包ID")
    private Long packageId;

    @TableField("sco_identifier")
    @Schema(description = "SCO标识")
    private String scoIdentifier;

    @TableField("lesson_status")
    @Schema(description = "cmi.core.lesson_status")
    private String lessonStatus;

    @TableField("score_raw")
    @Schema(description = "cmi.core.score.raw")
    private BigDecimal scoreRaw;

    @TableField("score_min")
    @Schema(description = "cmi.core.score.min")
    private BigDecimal scoreMin;

    @TableField("score_max")
    @Schema(description = "cmi.core.score.max")
    private BigDecimal scoreMax;

    @TableField("session_time_seconds")
    @Schema(description = "cmi.core.session_time 秒")
    private Integer sessionTimeSeconds;

    @TableField("total_time_seconds")
    @Schema(description = "cmi.core.total_time 秒")
    private Integer totalTimeSeconds;

    @TableField("created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
