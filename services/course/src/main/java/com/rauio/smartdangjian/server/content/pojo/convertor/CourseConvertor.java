package com.rauio.smartdangjian.server.content.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseConvertor {
    Course toCourse(CourseRequest courseRequest);

    CourseResponse toResponse(Course course);

    List<CourseResponse> toResponseList(List<Course> courses);
}
