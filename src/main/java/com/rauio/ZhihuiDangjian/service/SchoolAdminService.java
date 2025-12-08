package com.rauio.ZhihuiDangjian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    Page<User>      getUser(UserDto userDto, int pageNum, int pageSize);

}