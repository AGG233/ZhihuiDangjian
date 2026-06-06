package com.rauio.smartdangjian.server.ai.tool;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.search.api.SearchQueryFacade;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LearningPathTool {

    private final SearchQueryFacade searchQueryFacade;
    private final CurrentUserProvider currentUserProvider;

    @Tool(name = "getLearningProfile", description = "获取当前用户的学习画像数据（包含学习统计、知识掌握情况、答题统计等）")
    public Map<String, Object> getLearningProfile() {
        String userId = ToolContextUtil.resolveUserId(currentUserProvider);
        UserProfileResponse profile = searchQueryFacade.getProfile(userId);

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
