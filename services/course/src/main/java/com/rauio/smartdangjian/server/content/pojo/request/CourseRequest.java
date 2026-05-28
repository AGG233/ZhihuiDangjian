package com.rauio.smartdangjian.server.content.pojo.request;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "课程请求体")
public class CourseRequest {

    @Schema(description = "课程标题")
    @NotNull(message = "课程标题不能为空")
    private String title;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "课程封面图片ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long coverImageId;

    @Schema(description = "课程分类ID")
    @NotNull(message = "课程分类不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    @Schema(description = "课程难度")
    private String difficulty;

    @Schema(description = "课程预计时长")
    private Integer estimatedDuration;

    @Schema(description = "课程是否发布")
    private Boolean isPublished;
}
