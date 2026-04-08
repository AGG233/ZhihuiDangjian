package com.rauio.smartdangjian.server.learning.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("user_learning_record")
@Schema(description = "用户学习记录")
public class UserLearningRecord {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "记录ID")
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
