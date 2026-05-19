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

import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserInfoToolTest {

    @Mock
    private UserService userService;

    @Mock
    private UserConvertor userConvertor;

    @InjectMocks
    private UserInfoTool userInfoTool;

    @Test
    @DisplayName("getUserInfo 返回用户基本信息")
    void getUserInfo() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");

        User user = new User();
        user.setId("user-1");
        user.setUsername("testuser");

        UserVO userVO = new UserVO();
        userVO.setId("user-1");
        userVO.setUsername("testuser");

        when(userService.getById("user-1")).thenReturn(user);
        when(userConvertor.toVO(user)).thenReturn(userVO);

        UserVO result = userInfoTool.getUserInfo(toolContext);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("user-1");
        assertThat(result.getUsername()).isEqualTo("testuser");
    }
}
