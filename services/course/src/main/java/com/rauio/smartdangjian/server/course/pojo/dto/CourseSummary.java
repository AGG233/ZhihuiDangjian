package com.rauio.smartdangjian.server.course.pojo.dto;

import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果中的课程摘要 —— 不暴露课程实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummary {
    private Long id;
    private String title;
    private String description;

    public static CourseSummary from(CourseResponse response) {
        if (response == null) return null;
        return CourseSummary.builder()
                .id(response.getId())
                .title(response.getTitle())
                .description(response.getDescription())
                .build();
    }
}
