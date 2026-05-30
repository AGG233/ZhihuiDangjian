package com.rauio.smartdangjian.server.learning.mapper.dto;

import lombok.Data;

/**
 * 学习趋势原始查询结果，用于 MyBatis 结果映射
 */
@Data
public class TrendRaw {
    /**
     * MySQL DATE() 函数返回字符串格式如 "2026-05-30"
     */
    private String date;
    private Integer count;
}
