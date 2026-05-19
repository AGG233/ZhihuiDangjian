package com.rauio.smartdangjian.server.content.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChapterConvertor {
    ChapterRequest toRequest(Chapter chapter);

    Chapter toEntity(ChapterRequest chapterRequest);

    ChapterResponse toResponse(Chapter chapter);

    List<ChapterResponse> toResponseList(List<Chapter> chapterList);
}
