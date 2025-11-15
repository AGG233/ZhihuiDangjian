package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.Category;
import com.rauio.ZhihuiDangjian.pojo.dto.CategoryDto;
import com.rauio.ZhihuiDangjian.pojo.vo.CategoryVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryConvertor {
    Category toEntity(CategoryDto categoryDto);
    CategoryDto toCourseCategoryDto(Category category);
    CategoryVO toVO(Category categoryDto);
    List<CategoryVO>  toVOList(List<Category> categoryList);
}