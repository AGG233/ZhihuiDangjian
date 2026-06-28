package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.user.api.UserProfileQueryFacade;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;

@ExtendWith(MockitoExtension.class)
class UserInfoToolTest {

    @Mock
    private UserProfileQueryFacade userProfileQueryFacade;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UserInfoTool userInfoTool;

    @Test
    @DisplayName("getUserInfo 返回用户基本信息")
    void getUserInfo() {
        UserResponse userVO = new UserResponse();
        userVO.setId(1L);
        userVO.setUsername("testuser");

        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        when(userProfileQueryFacade.getUserById("1")).thenReturn(userVO);

        UserResponse result = userInfoTool.getUserInfo();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getUserInfo 用户不存在时返回 null")
    void getUserInfoUserNotFound() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("999");
        when(userProfileQueryFacade.getUserById("999")).thenReturn(null);

        UserResponse result = userInfoTool.getUserInfo();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUserInfo 当前用户 ID 为 null 时以 null 查询并返回 null")
    void getUserInfoWithNullUserId() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(null);
        when(userProfileQueryFacade.getUserById(null)).thenReturn(null);

        UserResponse result = userInfoTool.getUserInfo();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUserInfo 返回的用户包含 realName 字段")
    void getUserInfoContainsRealName() {
        UserResponse userVO = new UserResponse();
        userVO.setId(2L);
        userVO.setUsername("zhangsan");
        userVO.setRealName("张三");

        when(currentUserProvider.getCurrentUserId()).thenReturn("2");
        when(userProfileQueryFacade.getUserById("2")).thenReturn(userVO);

        UserResponse result = userInfoTool.getUserInfo();

        assertThat(result.getRealName()).isEqualTo("张三");
    }
}
