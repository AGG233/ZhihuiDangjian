package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.UserLearningRecord;
import com.rauio.ZhihuiDangjian.pojo.dto.UserLearningRecordDto;
import com.rauio.ZhihuiDangjian.pojo.vo.UserLearningRecordVO;
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
