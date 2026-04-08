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
@TableName("user_chapter_progress")
@Schema(description = "用户章节学习进度")
public class UserChapterProgress {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "进度ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "章节ID")
    private String chapterId;

    @Schema(description = "学习进度（0-100）", example = "75")
    private Integer progress;

    @Schema(description = "学习状态：not_started表示未开始，in_progress表示学习中，completed表示已完成", allowableValues = {"not_started", "in_progress", "completed"}, example = "in_progress")
    private String status;

    @Schema(description = "首次观看时间")
    private LocalDateTime firstViewedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

}
