package com.rauio.smartdangjian.server.chapter.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.chapter.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.chapter.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.chapter.pojo.response.ChapterResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChapterConvertor {
    ChapterRequest toRequest(Chapter chapter);

    Chapter toEntity(ChapterRequest chapterRequest);

    ChapterResponse toResponse(Chapter chapter);

    List<ChapterResponse> toResponseList(List<Chapter> chapterList);
}
