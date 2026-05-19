package com.rauio.smartdangjian.server.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
