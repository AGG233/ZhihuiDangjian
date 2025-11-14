package com.rauio.ZhihuiDangjiang.pojo.dto;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

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
