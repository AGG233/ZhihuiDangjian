package com.rauio.smartdangjian.server.content.pojo.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.rauio.smartdangjian.server.content.pojo.entity.Course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "课程视图对象")
public class CourseResponse {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "课程分类ID，当前业务通过分类关联表维护")
    private Long categoryId;

    @Schema(description = "课程难度")
    private String difficulty;

    @Schema(description = "课程封面资源ID")
    private Long coverImageId;

    @Schema(description = "预计学习时长（分钟）")
    private Integer estimatedDuration;

    @Schema(description = "报名人数")
    private Integer enrollmentCount;

    @Schema(description = "平均评分")
    private BigDecimal averageRating;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(description = "创建者ID")
    private Long creatorId;

    public static CourseResponse from(Course course) {
        if (course == null) {
            return null;
        }
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .difficulty(course.getDifficulty())
                .coverImageId(course.getCoverImageId())
                .estimatedDuration(course.getEstimatedDuration())
                .enrollmentCount(course.getEnrollmentCount())
                .averageRating(course.getAverageRating())
                .publishedAt(course.getPublishedAt())
                .creatorId(course.getCreatorId())
                .build();
    }
}
