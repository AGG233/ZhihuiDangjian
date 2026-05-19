package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.search.service.UserProfileService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserProfileToolTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserProfileTool userProfileTool;

    @Test
    @DisplayName("getUserProfile 返回用户画像数据")
    void getUserProfile() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");

        UserProfileResponse profile = mock(UserProfileResponse.class);
        when(userProfileService.getProfile("user-1")).thenReturn(profile);

        UserProfileResponse result = userProfileTool.getUserProfile(toolContext);

        assertThat(result).isSameAs(profile);
    }
}
