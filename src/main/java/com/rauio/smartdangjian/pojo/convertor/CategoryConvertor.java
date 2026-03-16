package com.rauio.smartdangjian.pojo.convertor;

import com.rauio.smartdangjian.pojo.Category;
import com.rauio.smartdangjian.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.pojo.vo.CategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryConvertor {
    Category toEntity(CategoryDto categoryDto);
    CategoryDto toCourseCategoryDto(Category category);
    CategoryVO toVO(Category categoryDto);
    List<CategoryVO>  toVOList(List<Category> categoryList);
}