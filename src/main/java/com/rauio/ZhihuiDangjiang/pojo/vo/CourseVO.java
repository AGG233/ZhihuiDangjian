package com.rauio.ZhihuiDangjiang.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
