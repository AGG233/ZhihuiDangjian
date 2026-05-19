package com.rauio.smartdangjian.server.content.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChapterConvertor {
    ChapterDto toDto(Chapter chapter);

    Chapter toEntity(ChapterDto chapterDto);

    ChapterVO toVO(Chapter chapter);

    List<ChapterVO> toVOList(List<Chapter> chapterList);
}
