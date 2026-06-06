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
}
