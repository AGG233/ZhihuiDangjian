package com.rauio.smartdangjian.pojo.convertor;

import com.rauio.smartdangjian.pojo.Course;
import com.rauio.smartdangjian.pojo.dto.CourseDto;
import com.rauio.smartdangjian.pojo.vo.CourseVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseConvertor {
    Course toCourse(CourseDto courseDto);
    CourseVO toVO(Course course);
    List<CourseVO> toVOList(List<Course> courses);

}