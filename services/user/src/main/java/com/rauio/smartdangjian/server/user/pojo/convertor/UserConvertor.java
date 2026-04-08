package com.rauio.smartdangjian.server.user.pojo.convertor;

import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.vo.UserPublicVO;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvertor {
    UserVO toVO(User user);

    UserPublicVO toPublicVO(User user);

    User toEntity(UserDto userDto);
    User toEntity(UserVO userVO);

    UserDto toDto(User user);

    List<UserVO> toVO(List<User> userList);
    List<UserPublicVO> toPublicVO(List<User> userList);
    List<User> toEntityList(List<UserDto> userList);
}
