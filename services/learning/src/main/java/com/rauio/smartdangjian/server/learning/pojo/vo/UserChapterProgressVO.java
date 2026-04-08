package com.rauio.smartdangjian.server.learning.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@Schema(description = "用户章节进度视图对象")
public class UserChapterProgressVO {
    @Schema(description = "进度ID")
    private String id;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "章节ID")
    private String chapterId;
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
