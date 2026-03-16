package com.rauio.smartdangjian.pojo.convertor;

import com.rauio.smartdangjian.pojo.UserLearningRecord;
import com.rauio.smartdangjian.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.pojo.vo.UserLearningRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserLearningRecordConvertor {
    UserLearningRecordDto toDto(UserLearningRecord userLearningRecord);
    UserLearningRecord toEntity(UserLearningRecordDto dto);
    UserLearningRecordVO toVO(UserLearningRecord userLearningRecord);
    List<UserLearningRecordVO> toVOList(List<UserLearningRecord> list);
}
