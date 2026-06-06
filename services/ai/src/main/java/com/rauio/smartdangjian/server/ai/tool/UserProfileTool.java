package com.rauio.smartdangjian.server.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.search.api.SearchQueryFacade;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserProfileTool {

    private final SearchQueryFacade searchQueryFacade;
    private final CurrentUserProvider currentUserProvider;

    @Tool(description = "获取当前用户的学习画像，包括学习统计、知识掌握程度、兴趣分类、答题统计等信息，用于个性化推荐和学习建议")
    public UserProfileResponse getUserProfile() {
        String userId = ToolContextUtil.resolveUserId(currentUserProvider);
        return searchQueryFacade.getProfile(userId);
    }
}
