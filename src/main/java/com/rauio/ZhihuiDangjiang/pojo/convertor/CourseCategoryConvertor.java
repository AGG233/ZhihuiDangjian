package com.rauio.ZhihuiDangjiang.pojo.convertor;

import com.rauio.ZhihuiDangjiang.pojo.CourseCategory;
import com.rauio.ZhihuiDangjiang.pojo.dto.CourseCategoryDto;
import com.rauio.ZhihuiDangjiang.pojo.vo.CourseCategoryVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseCategoryConvertor {
    CourseCategory          toEntity(CourseCategoryDto courseCategoryDto);
    CourseCategoryDto       toCourseCategoryDto(CourseCategory courseCategory);
    CourseCategoryVO        toVO(CourseCategory courseCategoryDto);
    List<CourseCategoryVO>  toVOList(List<CourseCategory> courseCategoryList);
}