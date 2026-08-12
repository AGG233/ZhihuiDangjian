package com.rauio.smartdangjian.server.learning.pojo.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "用户学习记录请求体")
public class UserLearningRecordRequest {

    @Schema(description = "记录ID，更新时需要")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "章节ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chapterId;

    @Schema(description = "开始学习时间")
    private LocalDateTime startTime;

    @Schema(description = "结束学习时间")
    private LocalDateTime endTime;

    @Schema(description = "学习时长（秒）", example = "1800")
    private Integer duration;

    @Schema(
            description = "设备类型",
            allowableValues = {"web", "mobile", "tablet"},
            example = "web")
    private String deviceType;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
