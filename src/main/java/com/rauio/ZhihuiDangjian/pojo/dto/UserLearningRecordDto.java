package com.rauio.ZhihuiDangjian.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@Schema(description = "用户学习记录请求体")
public class UserLearningRecordDto {

    @Schema(description = "记录ID，更新时需要")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "开始学习时间")
    private Date startTime;

    @Schema(description = "结束学习时间")
    private Date endTime;

    @Schema(description = "学习时长（秒）", example = "1800")
    private Integer duration;

    @Schema(description = "设备类型", allowableValues = {"web", "mobile", "tablet"}, example = "web")
    private String deviceType;

    @Schema(description = "创建时间")
    private Date createdAt;
}
