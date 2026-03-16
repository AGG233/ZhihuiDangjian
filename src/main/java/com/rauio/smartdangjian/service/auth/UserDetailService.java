package com.rauio.smartdangjian.service.auth;

import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String passport) throws UsernameNotFoundException {
        User user = userService.getUserByAll(passport);
        if (user != null) {
            return user;
        } else {
            throw new UsernameNotFoundException("用户不存在");
        }
    }


}
