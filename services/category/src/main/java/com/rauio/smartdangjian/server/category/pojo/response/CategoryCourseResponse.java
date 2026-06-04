package com.rauio.smartdangjian.server.category.pojo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分类-课程关联响应")
public record CategoryCourseResponse(
        @Schema(description = "分类ID") Long categoryId,
        @JsonSerialize(using = ToStringSerializer.class) @Schema(description = "课程ID") Long courseId) {

    public static CategoryCourseResponse from(CategoryCourse relation) {
        if (relation == null) {
            return null;
        }
        return new CategoryCourseResponse(relation.getCategoryId(), relation.getCourseId());
    }
}
