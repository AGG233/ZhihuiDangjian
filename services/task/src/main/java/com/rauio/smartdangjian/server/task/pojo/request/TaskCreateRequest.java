package com.rauio.smartdangjian.server.task.pojo.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.rauio.smartdangjian.server.task.spec.TaskType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "创建任务请求")
public class TaskCreateRequest {

    @NotBlank(message = "任务标题不能为空")
    @Schema(description = "任务标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @NotNull(message = "任务类型不能为空")
    @Schema(
            description = "任务类型: learning=学习, quiz=测验, social=社会实践, custom=自定义",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private TaskType taskType;

    @Schema(description = "任务积分", defaultValue = "0")
    private Integer points;

    @Schema(description = "任务截止时间")
    private LocalDateTime deadline;
}
