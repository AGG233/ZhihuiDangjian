package com.rauio.smartdangjian.server.content.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.pojo.request.CategoryRequest;
import com.rauio.smartdangjian.server.content.pojo.response.CategoryResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryConvertor {
    Category toEntity(CategoryRequest categoryRequest);

    CategoryRequest toCourseCategoryRequest(Category category);

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categoryList);
}
