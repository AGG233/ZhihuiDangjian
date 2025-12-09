package com.rauio.ZhihuiDangjian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;

import java.util.List;

public interface AdminService {

    // 系统管理员功能
    Boolean addUser(List<UserDto> user);
    Boolean deleteUser(List<String> idList);
    Boolean updateUser(List<UserDto> user);

    Page<User> getUser(UserDto userDto, int pageNum, int pageSize);
}