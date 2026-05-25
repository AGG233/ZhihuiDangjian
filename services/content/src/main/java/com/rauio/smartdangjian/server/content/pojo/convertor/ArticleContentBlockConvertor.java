package com.rauio.smartdangjian.server.content.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.entity.ArticleContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleContentBlockConvertor {

    @Mapping(source = "articleId", target = "parentId")
    ContentBlockResponse toResponse(ArticleContentBlock entity);

    List<ContentBlockResponse> toResponseList(List<ArticleContentBlock> entities);

    ArticleContentBlock toEntity(ContentBlockDto dto);

    ContentBlockDto toDto(ArticleContentBlock entity);
}
