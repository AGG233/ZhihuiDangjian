package com.rauio.ZhihuiDangjian.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class CourseVO {
    private String id;
    private String title;
    private String description;
    private String categoryId;
    private String difficulty;
    private String coverImageHash;
    private Integer estimatedDuration;
    private Integer enrollmentCount;
    private BigDecimal averageRating;
    private Date publishedAt;
    private String creatorId;
}
