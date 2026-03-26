package com.rauio.smartdangjian.server.content.pojo.convertor;

import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.server.content.pojo.vo.CategoryVO;
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
