package com.rauio.smartdangjian.server.ai.tool;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.search.service.RecommendService;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendTool {

    private final RecommendService recommendService;
    private final UserService userService;

    @Tool(description = "为当前用户获取个性化推荐课程ID列表，基于协同过滤、知识图谱和用户画像综合推荐")
    public String getRecommendedCourses(
            @ToolParam(description = "返回推荐数量，默认10") Integer limit) {
        String userId = userService.getCurrentUserId();
        int size = limit != null && limit > 0 ? limit : 10;
        Page<String> result = recommendService.recommend(userId, 1, size);
        if (result.getRecords().isEmpty()) {
            return "暂无推荐课程，请先完成更多学习内容以获取个性化推荐";
        }
        return "推荐课程ID列表: " + String.join(", ", result.getRecords());
    }
}
