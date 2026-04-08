package com.rauio.smartdangjian.server.content.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@TableName("course")
@Schema(description = "课程")
public class Course {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "课程ID")
    private String id;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "课程封面图片哈希")
    private String coverImageHash;

    @Schema(description = "课程分类ID")
    private String categoryId;

    @Schema(description = "课程难度")
    private String difficulty;

    @Schema(description = "预计学习时长（秒）")
    private Integer estimatedDuration;

    @Schema(description = "创建者ID")
    private String creatorId;

    @Schema(description = "报名人数")
    private Integer enrollmentCount;

    @Schema(description = "平均评分")
    private BigDecimal averageRating;

    @Schema(description = "是否已发布")
    private Boolean isPublished;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
