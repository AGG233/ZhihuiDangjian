package com.rauio.smartdangjian.server.content.pojo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "课程请求体")
public class CourseDto {

    @Schema(description = "课程标题")
    @NotBlank(message = "课程标题不能为空")
    private String title;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "课程封面图片ID")
    private String coverImageId;

    @Schema(description = "课程分类ID")
    @NotBlank(message = "课程分类不能为空")
    private String categoryId;

    @Schema(description = "课程难度")
    private String difficulty;

    @Schema(description = "课程预计时长")
    private Integer estimatedDuration;
    
    @Schema(description = "课程是否发布")
    private Boolean isPublished;
}
