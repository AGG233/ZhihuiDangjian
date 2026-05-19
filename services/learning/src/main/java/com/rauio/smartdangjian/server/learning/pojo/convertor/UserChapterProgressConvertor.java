package com.rauio.smartdangjian.server.learning.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.learning.pojo.dto.UserChapterProgressDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserChapterProgressVO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserChapterProgressConvertor {
    UserChapterProgressDto toDto(UserChapterProgress userChapterProgress);

    UserChapterProgress toEntity(UserChapterProgressDto dto);

    UserChapterProgressVO toVO(UserChapterProgress userChapterProgress);

    List<UserChapterProgressVO> toVOList(List<UserChapterProgress> list);
}
