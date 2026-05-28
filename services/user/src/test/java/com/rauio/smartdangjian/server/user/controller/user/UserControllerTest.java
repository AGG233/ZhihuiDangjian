package com.rauio.smartdangjian.server.user.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.server.user.pojo.response.UserPublicResponse;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @Test
    @DisplayName("get 根据用户ID返回用户视图")
    void get() {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("testuser");
        when(userService.get(1L)).thenReturn(response);

        var result = controller.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo(response);
        verify(userService).get(1L);
    }

    @Test
    @DisplayName("getPage 委托 service 分页查询公开用户信息")
    void getPage() {
        UserRequest dto = new UserRequest();
        Page<UserPublicResponse> page = new Page<>(1, 10);
        when(userService.getPage(any(UserRequest.class), anyInt(), anyInt())).thenReturn(page);

        var result = controller.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo(page);
        verify(userService).getPage(dto, 1, 10);
    }

    @Test
    @DisplayName("getPage 使用默认分页参数")
    void getPageWithDefaults() {
        UserRequest dto = new UserRequest();
        Page<UserPublicResponse> page = new Page<>(1, 10);
        when(userService.getPage(any(UserRequest.class), anyInt(), anyInt())).thenReturn(page);

        var result = controller.getPage(dto, 0, 0);

        assertThat(result.getCode()).isEqualTo("200");
        verify(userService).getPage(dto, 0, 0);
    }

    @Test
    @DisplayName("update 委托 service 更新用户信息")
    void update() {
        User user = User.builder().realName("新名字").build();
        when(userService.update(1L, user)).thenReturn(user);

        var result = controller.update(1L, user);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        verify(userService).update(1L, user);
    }

    @Test
    @DisplayName("delete 返回固定错误响应")
    void delete() {
        var result = controller.delete(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("404");
        assertThat(result.getMessage()).isEqualTo("接口已经弃用");
        assertThat(result.getData()).isNull();
    }
}
