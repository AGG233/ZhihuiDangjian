package com.rauio.smartdangjian.server.learning.mapper.dto;

import lombok.Data;

/**
 * 热门课程原始查询结果，用于 MyBatis 结果映射
 */
@Data
public class HotCourseRaw {
    private Long courseId;
    private String courseTitle;
    private Integer learnerCount;
}
