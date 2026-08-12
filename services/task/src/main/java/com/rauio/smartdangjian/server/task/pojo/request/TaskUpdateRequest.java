package com.rauio.smartdangjian.server.task.pojo.request;

import java.time.LocalDateTime;

import com.rauio.smartdangjian.server.task.spec.TaskStatus;
import com.rauio.smartdangjian.server.task.spec.TaskType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "更新任务请求")
public class TaskUpdateRequest {

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "任务类型: learning=学习, quiz=测验, social=社会实践, custom=自定义")
    private TaskType taskType;

    @Schema(description = "任务积分")
    private Integer points;

    @Schema(description = "任务截止时间")
    private LocalDateTime deadline;

    @Schema(description = "任务状态: draft=草稿, published=已发布, closed=已关闭（draft→published→closed 单向流转）")
    private TaskStatus status;
}
