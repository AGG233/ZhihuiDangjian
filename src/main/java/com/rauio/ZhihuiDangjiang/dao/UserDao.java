package com.rauio.ZhihuiDangjiang.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rauio.ZhihuiDangjiang.mapper.UserMapper;
import com.rauio.ZhihuiDangjiang.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao{

    private final UserMapper userMapper;

    @Autowired
    public UserDao(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    public User get(String id) {
        return userMapper.selectById(id);
    }
    public Boolean update(User user) {
        return userMapper.updateById(user) > 0;
    }
    public Boolean insert(User user) {
        return userMapper.insert(user) > 0;
    }
    public Boolean delete(String user_id) {
        return userMapper.deleteById(user_id) > 0;
    }

    public Boolean changePassword(String id,String password){
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(User::getPassword, password).eq(User::getId, id);
        return userMapper.update(null, wrapper) > 0;
    }
    public User getUserByName(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getUsername,username);
        return this.userMapper.selectOne(queryWrapper);
    }

    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getEmail, email);
        return this.userMapper.selectOne(queryWrapper);
    }

    public User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getPhone, phone);
        return this.userMapper.selectOne(queryWrapper);
    }

    public User getUserByPartyMemberId(String partyMemberId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getPartyMemberId, partyMemberId);
        return this.userMapper.selectOne(queryWrapper);
    }
}
