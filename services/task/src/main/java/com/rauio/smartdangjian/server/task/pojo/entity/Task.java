package com.rauio.smartdangjian.server.task.pojo.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.server.task.spec.TaskStatus;
import com.rauio.smartdangjian.server.task.spec.TaskType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("task")
@Schema(description = "任务")
public class Task {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @EnumValue
    @Schema(description = "任务类型: learning=学习, quiz=测验, social=社会实践, custom=自定义")
    private TaskType taskType;

    @Schema(description = "任务积分")
    private Integer points;

    @Schema(description = "任务截止时间")
    private LocalDateTime deadline;

    @Schema(description = "创建者（学校/管理员）用户ID")
    private Long creatorId;

    @EnumValue
    @Schema(description = "任务状态: draft=草稿, published=已发布, closed=已关闭")
    private TaskStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
