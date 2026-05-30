package com.rauio.smartdangjian.server.learning.mapper.dto;

import lombok.Data;

/**
 * 热门分类原始查询结果，用于 MyBatis 结果映射
 */
@Data
public class HotCategoryRaw {
    private Long categoryId;
    private String categoryName;
    private Integer learnerCount;
}
