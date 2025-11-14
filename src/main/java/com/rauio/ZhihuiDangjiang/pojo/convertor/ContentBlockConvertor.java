package com.rauio.ZhihuiDangjiang.pojo.convertor;

import com.rauio.ZhihuiDangjiang.pojo.ContentBlock;
import com.rauio.ZhihuiDangjiang.pojo.dto.ContentBlockDto;
import com.rauio.ZhihuiDangjiang.pojo.vo.ContentBlockVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContentBlockConvertor {
    ContentBlock toEntity(ContentBlockDto contentBlockDto);
    ContentBlockDto toDto(ContentBlock contentBlock);
    ContentBlockVO  toVO(ContentBlock contentBlock);
    List<ContentBlockVO> toVOList(List<ContentBlock> entities);
}
