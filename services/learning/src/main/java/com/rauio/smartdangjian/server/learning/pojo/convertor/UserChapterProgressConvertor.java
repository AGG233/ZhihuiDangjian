package com.rauio.smartdangjian.server.learning.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.request.UserChapterProgressRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserChapterProgressConvertor {
    UserChapterProgressRequest toRequest(UserChapterProgress userChapterProgress);

    UserChapterProgress toEntity(UserChapterProgressRequest request);

    UserChapterProgressResponse toResponse(UserChapterProgress userChapterProgress);

    List<UserChapterProgressResponse> toResponseList(List<UserChapterProgress> list);
}
