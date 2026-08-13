package com.rauio.smartdangjian.server.task.pojo.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "提交任务请求")
public class TaskSubmitRequest {

    @NotNull(message = "完成进度不能为空")
    @Min(value = 0, message = "完成进度不能小于0")
    @Max(value = 100, message = "完成进度不能大于100")
    @Schema(description = "完成进度 0-100，达到100视为完成", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer progress;
}
