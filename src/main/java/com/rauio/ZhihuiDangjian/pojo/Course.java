package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * (Course)实体类
 *
 * @author makejava
 * @since 2024-09-06 11:06:42
 */
@Data
@Builder
@TableName("course")
public class Course implements Serializable {

    @TableId
    private String id;
    private String title;
    private String description;
    private String coverImageHash;
    private String categoryId;
    private String difficulty;
    private Integer estimatedDuration;
    private String creatorId;
    private Integer enrollmentCount;
    private BigDecimal averageRating;
    private Boolean isPublished;
    private Date publishedAt;
    private Date createdAt;
    private Date updatedAt;
}
