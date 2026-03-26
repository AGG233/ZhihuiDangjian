package com.rauio.smartdangjian.server.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 按登录凭证加载用户信息。
     *
     * @param passport 用户名、邮箱或手机号
     * @return Spring Security 用户详情
     * @throws UsernameNotFoundException 当用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String passport) throws UsernameNotFoundException {
        User user = getByPassport(passport);
        if (user != null) {
            return user;
        } else {
            throw new UsernameNotFoundException("用户不存在");
        }
    }

    private User getByPassport(String passport) {
        if (passport == null || passport.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (passport.contains("@")) {
            wrapper.eq(User::getEmail, passport);
        } else if (passport.contains("+")) {
            wrapper.eq(User::getPhone, passport);
        } else {
            wrapper.eq(User::getUsername, passport);
        }
        return userMapper.selectOne(wrapper);
    }
}
