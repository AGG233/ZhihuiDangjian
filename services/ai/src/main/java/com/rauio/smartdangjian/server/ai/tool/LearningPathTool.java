package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.search.service.UserProfileService;
import com.rauio.smartdangjian.search.service.RecommendService;
import com.rauio.smartdangjian.search.pojo.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LearningPathTool {

    private final UserProfileService userProfileService;
    private final UserService userService;

    @Tool(name = "getLearningProfile", description = "获取当前用户的学习画像数据（包含学习统计、知识掌握情况、答题统计等）")
    public Map<String, Object> getLearningProfile(ToolContext toolContext) {
        String userId = ToolContextUtil.getUserId(toolContext, userService);
        UserProfileVO profile = userProfileService.getProfile(userId);

        Map<String, Object> result = new HashMap<>();
        if (profile != null) {
            result.put("learningStats", profile.getLearning());
            result.put("knowledgeStats", profile.getKnowledge());
            result.put("interestCategoryIds", profile.getInterestCategoryIds());
            result.put("quizStats", profile.getQuiz());
        }
        return result;
    }
}
