package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.CourseCategory;
import com.rauio.ZhihuiDangjian.pojo.dto.CourseCategoryDto;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseCategoryVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseCategoryConvertor {
    CourseCategory          toEntity(CourseCategoryDto courseCategoryDto);
    CourseCategoryDto       toCourseCategoryDto(CourseCategory courseCategory);
    CourseCategoryVO        toVO(CourseCategory courseCategoryDto);
    List<CourseCategoryVO>  toVOList(List<CourseCategory> courseCategoryList);
}