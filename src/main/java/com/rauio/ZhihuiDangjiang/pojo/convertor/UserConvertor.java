package com.rauio.ZhihuiDangjiang.pojo.convertor;

import com.rauio.ZhihuiDangjiang.pojo.User;
import com.rauio.ZhihuiDangjiang.pojo.dto.UserDto;
import com.rauio.ZhihuiDangjiang.pojo.vo.UserVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConvertor {
    UserVO toVO(User user);

    User toEntity(UserVO userVO);

    UserDto toDto(User user);
}
