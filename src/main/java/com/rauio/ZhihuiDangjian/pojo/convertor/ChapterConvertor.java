package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.Chapter;
import com.rauio.ZhihuiDangjian.pojo.dto.ChapterDto;
import com.rauio.ZhihuiDangjian.pojo.vo.ChapterVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChapterConvertor {
    ChapterDto  toDto(Chapter chapter);
    Chapter     toEntity(ChapterDto chapterDto);
    ChapterVO   toVO(Chapter chapter);
    List<ChapterVO> toVOList(List<Chapter> chapterList);
}
