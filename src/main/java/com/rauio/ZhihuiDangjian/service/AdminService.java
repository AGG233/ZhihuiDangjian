package com.rauio.ZhihuiDangjian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AdminService {

    String addUser(@RequestBody List<UserDto> user);
    String deleteUser(@RequestBody List<String> idList);
    String updateUser(@RequestBody List<UserDto> user);

    String addSchoolAdmin(@RequestBody List<UserDto> user);
    String updateSchoolAdmin(@RequestBody List<UserDto> user);
    String deleteSchoolAdmin(@RequestBody List<String> idList);

    Page<User> getUser(UserDto userDto, int pageNum, int pageSize);
}