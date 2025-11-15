package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import com.rauio.ZhihuiDangjian.pojo.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvertor {
    UserVO toVO(User user);

    User toEntity(UserVO userVO);

    UserDto toDto(User user);
}
