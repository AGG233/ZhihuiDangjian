package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.Course;
import com.rauio.ZhihuiDangjian.pojo.dto.CourseDto;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseConvertor {
    Course      CourseDtoToCourse(CourseDto courseDto);
    CourseVO    CourseToCourseVO(Course course);
}
