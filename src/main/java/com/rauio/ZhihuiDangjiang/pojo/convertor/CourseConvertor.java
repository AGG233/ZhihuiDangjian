package com.rauio.ZhihuiDangjiang.pojo.convertor;

import com.rauio.ZhihuiDangjiang.pojo.Course;
import com.rauio.ZhihuiDangjiang.pojo.dto.CourseDto;
import com.rauio.ZhihuiDangjiang.pojo.vo.CourseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseConvertor {
    Course      CourseDtoToCourse(CourseDto courseDto);
    CourseVO    CourseToCourseVO(Course course);
}
