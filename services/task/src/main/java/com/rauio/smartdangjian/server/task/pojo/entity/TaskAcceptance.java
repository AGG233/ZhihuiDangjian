package com.rauio.smartdangjian.server.task.pojo.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.server.task.spec.TaskAcceptanceStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("task_acceptance")
@Schema(description = "任务领取记录")
public class TaskAcceptance {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "领取记录ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "领取用户ID")
    private Long userId;

    @Schema(description = "完成进度 0-100")
    private Integer progress;

    @EnumValue
    @Schema(description = "领取状态: accepted=已领取, in_progress=进行中, submitted=已提交, completed=已完成, rejected=已驳回")
    private TaskAcceptanceStatus status;

    @Schema(description = "领取时间")
    private LocalDateTime acceptedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
}
