package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.ContentBlock;
import com.rauio.ZhihuiDangjian.pojo.dto.ContentBlockDto;
import com.rauio.ZhihuiDangjian.pojo.vo.ContentBlockVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface ContentBlockConvertor {
    ContentBlock toEntity(ContentBlockDto contentBlockDto);
    ContentBlockDto toDto(ContentBlock contentBlock);
    ContentBlockVO  toVO(ContentBlock contentBlock);
    List<ContentBlockVO> toVOList(List<ContentBlock> entities);
}
