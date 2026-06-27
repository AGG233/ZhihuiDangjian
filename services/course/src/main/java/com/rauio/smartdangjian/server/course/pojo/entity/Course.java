package com.rauio.smartdangjian.server.course.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("course")
@Schema(description = "课程")
public class Course {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "课程描述")
    private String description;

    @TableField("cover_image_id")
    @Schema(description = "课程封面资源ID")
    private Long coverImageId;

    @Schema(description = "课程难度")
    private String difficulty;

    @Schema(description = "预计学习时长（分钟）")
    private Integer estimatedDuration;

    @Schema(description = "创建者ID")
    private Long creatorId;

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

    @TableField("like_count")
    @Schema(description = "点赞数")
    private Integer likeCount;
}
