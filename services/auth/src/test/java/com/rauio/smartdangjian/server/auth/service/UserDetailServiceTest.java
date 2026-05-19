package com.rauio.smartdangjian.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserDetailService userDetailService;

    @Test
    @DisplayName("loadUserByUsername passport 包含 @ 时按邮箱查询并返回 User")
    void loadUserByUsernameWithEmailPassport() {
        User expectedUser = User.builder()
                .id("u1")
                .username("testuser")
                .email("test@example.com")
                .build();
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(expectedUser);

        UserDetails result = userDetailService.loadUserByUsername("test@example.com");

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("loadUserByUsername passport 包含 + 时按手机号查询并返回 User")
    void loadUserByUsernameWithPhonePassport() {
        User expectedUser = User.builder()
                .id("u1")
                .username("testuser")
                .phone("+8613800138000")
                .build();
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(expectedUser);

        UserDetails result = userDetailService.loadUserByUsername("+8613800138000");

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("loadUserByUsername passport 为普通字符串时按用户名查询并返回 User")
    void loadUserByUsernameWithUsernamePassport() {
        User expectedUser = User.builder().id("u1").username("testuser").build();
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(expectedUser);

        UserDetails result = userDetailService.loadUserByUsername("testuser");

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("loadUserByUsername 用户不存在时抛出 UsernameNotFoundException")
    void loadUserByUsernameThrowsWhenUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> userDetailService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("用户不存在");
    }

    @Test
    @DisplayName("loadUserByUsername passport 为 null 时抛出 UsernameNotFoundException")
    void loadUserByUsernameThrowsWhenPassportIsNull() {
        assertThatThrownBy(() -> userDetailService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("loadUserByUsername passport 为空字符串时抛出 UsernameNotFoundException")
    void loadUserByUsernameThrowsWhenPassportIsEmpty() {
        assertThatThrownBy(() -> userDetailService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
