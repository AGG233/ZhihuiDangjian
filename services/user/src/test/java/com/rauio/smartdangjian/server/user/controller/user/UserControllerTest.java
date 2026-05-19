package com.rauio.smartdangjian.server.user.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.vo.UserPublicVO;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @Test
    @DisplayName("get 根据用户ID返回用户视图")
    void get() {
        UserVO vo = new UserVO();
        vo.setId("user-1");
        vo.setUsername("testuser");
        when(userService.get("user-1")).thenReturn(vo);

        var result = controller.get("user-1");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo(vo);
        verify(userService).get("user-1");
    }

    @Test
    @DisplayName("getPage 委托 service 分页查询公开用户信息")
    void getPage() {
        UserDto dto = new UserDto();
        Page<UserPublicVO> page = new Page<>(1, 10);
        when(userService.getPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

        var result = controller.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getData()).isEqualTo(page);
        verify(userService).getPage(dto, 1, 10);
    }

    @Test
    @DisplayName("getPage 使用默认分页参数")
    void getPageWithDefaults() {
        UserDto dto = new UserDto();
        Page<UserPublicVO> page = new Page<>(1, 10);
        when(userService.getPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

        var result = controller.getPage(dto, 0, 0);

        assertThat(result.getCode()).isEqualTo("200");
        verify(userService).getPage(dto, 0, 0);
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
    @DisplayName("delete 返回固定错误响应")
    void delete() {
        var result = controller.delete("user-1");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("404");
        assertThat(result.getMessage()).isEqualTo("接口已经弃用");
        assertThat(result.getData()).isNull();
    }
}
