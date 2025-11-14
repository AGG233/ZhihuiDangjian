package com.rauio.ZhihuiDangjian.pojo.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseDto {
    private String title;
    private String description;
    private String coverImageHash;
    private String categoryId;
    private String difficulty;
    private Integer estimatedDuration;
    private Boolean isPublished;
}
