package com.rauio.ZhihuiDangjian.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@Schema(description = "用户章节进度请求体")
public class UserChapterProgressDto {

    @Schema(description = "进度ID，更新时需要")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "学习进度（0-100）", example = "75")
    private Integer progress;

    @Schema(description = "学习状态", allowableValues = {"not_started", "in_progress", "completed"}, example = "in_progress")
    private String status;

    @Schema(description = "首次观看时间")
    private Date firstViewedAt;

    @Schema(description = "完成时间")
    private Date completedAt;

    @Schema(description = "更新时间")
    private Date updatedAt;
}
