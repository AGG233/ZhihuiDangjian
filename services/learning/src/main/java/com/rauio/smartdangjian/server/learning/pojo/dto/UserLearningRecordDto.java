package com.rauio.smartdangjian.server.learning.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "用户学习记录请求体")
public class UserLearningRecordDto {

    @Schema(description = "记录ID，更新时需要")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "章节ID")
    private String chapterId;

    @Schema(description = "开始学习时间")
    private LocalDateTime startTime;

    @Schema(description = "结束学习时间")
    private LocalDateTime endTime;

    @Schema(description = "学习时长（秒）", example = "1800")
    private Integer duration;

    @Schema(description = "设备类型", allowableValues = {"web", "mobile", "tablet"}, example = "web")
    private String deviceType;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
