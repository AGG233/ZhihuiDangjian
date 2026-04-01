package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.search.pojo.vo.UserProfileVO;
import com.rauio.smartdangjian.search.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileTool {

    private final UserProfileService userProfileService;

    @Tool(description = "获取当前用户的学习画像，包括学习统计、知识掌握程度、兴趣分类、答题统计等信息，用于个性化推荐和学习建议")
    public UserProfileVO getUserProfile() {
        return userProfileService.getCurrentUserProfile();
    }
}
