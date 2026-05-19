package com.rauio.smartdangjian.server.content.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.entity.ContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContentBlockConvertor {
    ContentBlock toEntity(ContentBlockDto contentBlockDto);

    ContentBlockDto toDto(ContentBlock contentBlock);

    ContentBlockResponse toResponse(ContentBlock contentBlock);

    List<ContentBlockResponse> toResponseList(List<ContentBlock> entities);
}
