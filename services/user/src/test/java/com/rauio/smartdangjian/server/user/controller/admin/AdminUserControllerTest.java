package com.rauio.smartdangjian.server.user.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController controller;

    @Test
    @DisplayName("get 根据用户ID返回用户详情")
    void get() {
        User user = User.builder().id("user-1").username("admin").build();
        when(userService.getById("user-1")).thenReturn(user);

        var result = controller.get("user-1");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo(user);
        verify(userService).getById("user-1");
    }

    @Test
    @DisplayName("getPage 委托 service 分页查询用户")
    void getPage() {
        UserDto dto = new UserDto();
        Page<User> page = new Page<>(1, 10);
        when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

        var result = controller.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo(page);
        verify(userService).getAdminPage(dto, 1, 10);
    }

    @Test
    @DisplayName("getPage 使用默认分页参数")
    void getPageWithDefaults() {
        UserDto dto = new UserDto();
        Page<User> page = new Page<>(1, 10);
        when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

        var result = controller.getPage(dto, 0, 0);

        assertThat(result.getCode()).isEqualTo("200");
        verify(userService).getAdminPage(dto, 0, 0);
    }

    @Test
    @DisplayName("create 委托 service 注册新用户")
    void create() {
        User user = User.builder().username("newuser").build();
        when(userService.register(user)).thenReturn(true);

        var result = controller.create(user);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isTrue();
        verify(userService).register(user);
    }

    @Test
    @DisplayName("create 注册失败时返回false")
    void createFails() {
        User user = User.builder().username("newuser").build();
        when(userService.register(user)).thenReturn(false);

        var result = controller.create(user);

        assertThat(result.getData()).isFalse();
    }

    @Test
    @DisplayName("update 委托 service 更新用户信息")
    void update() {
        User user = User.builder().realName("新名字").build();
        when(userService.update("user-1", user)).thenReturn(true);

        var result = controller.update("user-1", user);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isTrue();
        verify(userService).update("user-1", user);
    }

    @Test
    @DisplayName("update 更新失败时返回false")
    void updateFails() {
        User user = User.builder().realName("新名字").build();
        when(userService.update("user-1", user)).thenReturn(false);

        var result = controller.update("user-1", user);

        assertThat(result.getData()).isFalse();
    }
}
