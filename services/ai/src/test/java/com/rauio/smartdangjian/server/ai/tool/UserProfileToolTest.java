package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.search.pojo.vo.UserProfileVO;
import com.rauio.smartdangjian.search.service.UserProfileService;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        UserProfileVO profile = mock(UserProfileVO.class);
        when(userProfileService.getProfile("user-1")).thenReturn(profile);

        UserProfileVO result = userProfileTool.getUserProfile(toolContext);

        assertThat(result).isSameAs(profile);
    }
}
