package com.rauio.smartdangjian.server.content.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChapterContentBlockConvertor {

    @Mapping(source = "chapterId", target = "parentId")
    ContentBlockResponse toResponse(ChapterContentBlock entity);

    List<ContentBlockResponse> toResponseList(List<ChapterContentBlock> entities);

    ChapterContentBlock toEntity(ContentBlockDto dto);

    ContentBlockDto toDto(ChapterContentBlock entity);
}
