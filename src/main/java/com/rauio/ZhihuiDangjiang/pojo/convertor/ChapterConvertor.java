package com.rauio.ZhihuiDangjiang.pojo.convertor;

import com.rauio.ZhihuiDangjiang.pojo.Chapter;
import com.rauio.ZhihuiDangjiang.pojo.dto.ChapterDto;
import com.rauio.ZhihuiDangjiang.pojo.vo.ChapterVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChapterConvertor {
    ChapterDto  toDto(Chapter chapter);
    Chapter     toEntity(ChapterDto chapterDto);
    ChapterVO   toVO(Chapter chapter);
    List<ChapterVO> toVOList(List<Chapter> chapterList);
}
