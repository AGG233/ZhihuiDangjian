package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SchoolAdminService {

    int             addUser(@RequestBody List<UserDto> userDtoList);
    int             updateUser(@RequestBody List<UserDto> userDto);
    int             deleteUser(@RequestBody List<UserDto> userIdList);
    User            getUser(@RequestParam String id);
    List<User>      getUser(@RequestBody UserDto userDto);

}
