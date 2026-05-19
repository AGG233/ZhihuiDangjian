package com.rauio.smartdangjian.server.content.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.pojo.vo.CategoryVO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryConvertor {
    Category toEntity(CategoryDto categoryDto);

    CategoryDto toCourseCategoryDto(Category category);

    CategoryVO toVO(Category categoryDto);

    List<CategoryVO> toVOList(List<Category> categoryList);
}
