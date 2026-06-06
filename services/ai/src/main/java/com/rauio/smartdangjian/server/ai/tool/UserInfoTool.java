package com.rauio.smartdangjian.server.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.user.api.UserProfileQueryFacade;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserInfoTool {

    private final UserProfileQueryFacade userProfileQueryFacade;
    private final CurrentUserProvider currentUserProvider;

    @Tool(description = "获取用户基本信息")
    public UserResponse getUserInfo() {
        String userId = ToolContextUtil.resolveUserId(currentUserProvider);
        return userProfileQueryFacade.getUserById(userId);
    }
}
