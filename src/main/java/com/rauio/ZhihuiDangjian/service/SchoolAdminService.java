package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import com.rauio.ZhihuiDangjian.pojo.vo.UserVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface SchoolAdminService {

    int addUser(@RequestBody List<UserDto> userDtoList);
    int updateUser(@RequestBody List<UserDto> userDto);
    int deleteUser(@RequestBody List<String> userIdList);
    UserVO getUser(@RequestBody UserDto userDto);

}
