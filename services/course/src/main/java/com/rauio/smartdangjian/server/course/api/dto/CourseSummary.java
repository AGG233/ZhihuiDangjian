package com.rauio.smartdangjian.server.course.api.dto;

import com.rauio.smartdangjian.server.course.pojo.entity.Course;

import lombok.Builder;
import lombok.Data;

/**
 * 课程摘要 DTO。仅包含最小字段集，不暴露 isPublished 等内部状态。
 */
@Data
@Builder
public class CourseSummary {

    private Long id;

    private String title;

    private String description;

    private String difficulty;

    private Long coverImageId;

    private Integer enrollmentCount;

    /**
     * 从 Course 实体创建 CourseSummary。
     * 该方法编译时依赖 Course 实体（位于 pojo.entity 包），仅在门面实现层使用。
     *
     * @param course 课程实体
     * @return 课程摘要 DTO，若入参为 null 则返回 null
     */
    public static CourseSummary from(Course course) {
        if (course == null) {
            return null;
        }
        return CourseSummary.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .difficulty(course.getDifficulty())
                .coverImageId(course.getCoverImageId())
                .enrollmentCount(course.getEnrollmentCount())
                .build();
    }
}
