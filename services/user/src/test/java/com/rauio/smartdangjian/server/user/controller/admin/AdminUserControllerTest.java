package com.rauio.smartdangjian.server.user.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController controller;

    @Test
    @DisplayName("get 根据用户ID返回用户详情")
    void get() {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setUsername("admin");
        when(userService.get(1L)).thenReturn(user);

        var result = controller.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo(user);
        verify(userService).get(1L);
    }

    @Test
    @DisplayName("getPage 委托 service 分页查询用户")
    void getPage() {
        UserRequest dto = new UserRequest();
        Page<UserResponse> page = new Page<>(1, 10);
        when(userService.getAdminResponsePage(any(UserRequest.class), anyInt(), anyInt()))
                .thenReturn(page);

        var result = controller.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo(page);
        verify(userService).getAdminResponsePage(dto, 1, 10);
    }

    @Test
    @DisplayName("getPage 使用默认分页参数")
    void getPageWithDefaults() {
        UserRequest dto = new UserRequest();
        Page<UserResponse> page = new Page<>(1, 10);
        when(userService.getAdminResponsePage(any(UserRequest.class), anyInt(), anyInt()))
                .thenReturn(page);

        var result = controller.getPage(dto, 0, 0);

        assertThat(result.getCode()).isEqualTo("200");
        verify(userService).getAdminResponsePage(dto, 0, 0);
    }

    @Test
    @DisplayName("create 委托 service 注册新用户")
    void create() {
        UserRequest request = new UserRequest();
        request.setUsername("newuser");
        doNothing().when(userService).register(request);

        var result = controller.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        verify(userService).register(request);
    }

    @Test
    @DisplayName("update 委托 service 更新用户信息")
    void update() {
        UserRequest request = new UserRequest();
        request.setRealName("新名字");
        when(userService.update(1L, request)).thenReturn(null);

        var result = controller.update(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        verify(userService).update(1L, request);
    }
}
