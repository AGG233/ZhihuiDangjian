package com.rauio.smartdangjian.server.user.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.server.user.pojo.response.UserPublicResponse;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvertor {
    UserResponse toResponse(User user);

    UserPublicResponse toPublicResponse(User user);

    User toEntity(UserRequest userRequest);

    User toEntity(UserResponse userResponse);

    UserRequest toRequest(User user);

    List<UserResponse> toResponse(List<User> userList);

    List<UserPublicResponse> toPublicResponse(List<User> userList);

    List<User> toEntityList(List<UserRequest> userList);
}
