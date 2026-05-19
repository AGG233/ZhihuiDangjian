package com.rauio.smartdangjian.server.learning.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserLearningRecordConvertor {
    UserLearningRecordDto toDto(UserLearningRecord userLearningRecord);

    UserLearningRecord toEntity(UserLearningRecordDto dto);

    UserLearningRecordVO toVO(UserLearningRecord userLearningRecord);

    List<UserLearningRecordVO> toVOList(List<UserLearningRecord> list);
}
