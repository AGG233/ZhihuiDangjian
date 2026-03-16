package com.rauio.smartdangjian.pojo.convertor;

import com.rauio.smartdangjian.pojo.Chapter;
import com.rauio.smartdangjian.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.pojo.vo.ChapterVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface ChapterConvertor {
    ChapterDto  toDto(Chapter chapter);
    Chapter     toEntity(ChapterDto chapterDto);
    ChapterVO   toVO(Chapter chapter);
    List<ChapterVO> toVOList(List<Chapter> chapterList);
}
