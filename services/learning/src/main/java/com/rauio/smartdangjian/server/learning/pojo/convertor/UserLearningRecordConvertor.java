package com.rauio.smartdangjian.server.learning.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserLearningRecordConvertor {
    UserLearningRecordRequest toRequest(UserLearningRecord userLearningRecord);

    UserLearningRecord toEntity(UserLearningRecordRequest request);

    UserLearningRecordResponse toResponse(UserLearningRecord userLearningRecord);

    List<UserLearningRecordResponse> toResponseList(List<UserLearningRecord> list);
}
