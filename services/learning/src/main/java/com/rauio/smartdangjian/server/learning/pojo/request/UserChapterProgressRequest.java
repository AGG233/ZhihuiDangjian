package com.rauio.smartdangjian.server.learning.pojo.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "用户章节进度请求体")
public class UserChapterProgressRequest {

    @Schema(description = "进度ID，更新时需要")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "章节ID")
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @Schema(description = "学习进度（0-100）", example = "75")
    @Min(value = 0, message = "学习进度不能小于0")
    @Max(value = 100, message = "学习进度不能大于100")
    private Integer progress;

    @Schema(
            description = "学习状态：not_started表示未开始，in_progress表示学习中，completed表示已完成",
            allowableValues = {"not_started", "in_progress", "completed"},
            example = "in_progress")
    private String status;

    @Schema(description = "首次观看时间")
    private LocalDateTime firstViewedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
