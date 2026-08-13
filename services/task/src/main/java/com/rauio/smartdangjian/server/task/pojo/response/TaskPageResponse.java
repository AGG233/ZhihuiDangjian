package com.rauio.smartdangjian.server.task.pojo.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "任务分页响应")
public class TaskPageResponse {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "每页大小")
    private Long size;

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "任务列表")
    private List<TaskResponse> records;
}
