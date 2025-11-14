package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.ContentBlock;
import com.rauio.ZhihuiDangjian.pojo.dto.ContentBlockDto;
import com.rauio.ZhihuiDangjian.pojo.vo.ContentBlockVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContentBlockConvertor {
    ContentBlock toEntity(ContentBlockDto contentBlockDto);
    ContentBlockDto toDto(ContentBlock contentBlock);
    ContentBlockVO  toVO(ContentBlock contentBlock);
    List<ContentBlockVO> toVOList(List<ContentBlock> entities);
}
