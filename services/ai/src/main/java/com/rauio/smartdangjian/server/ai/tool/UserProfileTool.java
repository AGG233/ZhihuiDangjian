package com.rauio.smartdangjian.server.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.search.service.UserProfileService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserProfileTool {

    private final UserProfileService userProfileService;
    private final UserService userService;

    @Tool(description = "获取当前用户的学习画像，包括学习统计、知识掌握程度、兴趣分类、答题统计等信息，用于个性化推荐和学习建议")
    public UserProfileResponse getUserProfile(ToolContext toolContext) {
        String userId = ToolContextUtil.getUserId(toolContext, userService);
        return userProfileService.getProfile(userId);
    }
}
