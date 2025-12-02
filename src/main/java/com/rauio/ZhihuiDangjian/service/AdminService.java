package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AdminService {

    public String addUser(@RequestBody List<UserDto> user);
    public String deleteUser(@RequestBody List<String> idList);
    public String updateUser(@RequestBody List<UserDto> user);

    public String addSchoolAdmin(@RequestBody List<UserDto> user);
    public String updateSchoolAdmin(@RequestBody List<UserDto> user);
    public String deleteSchoolAdmin(@RequestBody List<String> idList);

    List<User> getUser(UserDto userDto);
}
