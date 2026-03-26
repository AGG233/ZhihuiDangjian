package com.rauio.smartdangjian.server.learning.pojo.convertor;

import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserChapterProgressDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserChapterProgressVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserChapterProgressConvertor {
    UserChapterProgressDto toDto(UserChapterProgress userChapterProgress);
    UserChapterProgress toEntity(UserChapterProgressDto dto);
    UserChapterProgressVO toVO(UserChapterProgress userChapterProgress);
    List<UserChapterProgressVO> toVOList(List<UserChapterProgress> list);
}
