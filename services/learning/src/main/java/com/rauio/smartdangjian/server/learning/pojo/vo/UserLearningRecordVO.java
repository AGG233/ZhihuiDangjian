package com.rauio.smartdangjian.server.learning.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@Schema(description = "用户学习记录视图对象")
public class UserLearningRecordVO {
    @Schema(description = "记录ID")
    private String id;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "章节ID")
    private String chapterId;
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
