package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.Chapter;
import com.rauio.ZhihuiDangjian.pojo.dto.ChapterDto;
import com.rauio.ZhihuiDangjian.pojo.vo.ChapterVO;
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
