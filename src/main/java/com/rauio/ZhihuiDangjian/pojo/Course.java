package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * (Course)实体类
 *
 * @author makejava
 * @since 2024-09-06 11:06:42
 */
@Data
@Builder
@TableName("course")
public class Course{

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
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
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}