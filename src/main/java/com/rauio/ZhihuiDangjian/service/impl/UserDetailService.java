package com.rauio.ZhihuiDangjian.service.impl;

import com.rauio.ZhihuiDangjian.pojo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserServiceImpl userServiceImpl;

    @Override
    public UserDetails loadUserByUsername(String passport) throws UsernameNotFoundException {
        User user = userServiceImpl.getUserByAll(passport);
        if (user != null) {
            return user;
        } else {
            throw new UsernameNotFoundException("用户不存在");
        }
    }


}
