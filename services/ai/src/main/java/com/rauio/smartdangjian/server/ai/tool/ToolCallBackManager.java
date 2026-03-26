package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.server.ai.tool.learning.LearnedCourseTool;
import com.rauio.smartdangjian.server.ai.tool.learning.RecentLearningRecordsTool;
import com.rauio.smartdangjian.server.ai.tool.user.UserInfoTool;
import com.rauio.smartdangjian.server.ai.tool.user.UserLearningRecordTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ToolCallBackManager {

    private final UserInfoTool userInfoTool;
    private final UserLearningRecordTool userLearningRecordTool;
    private final LearnedCourseTool learnedCourseTool;
    private final RecentLearningRecordsTool recentLearningRecordsTool;


    @Bean
    public ToolCallback userInfoToolCallBack() {
        return FunctionToolCallback
                .builder("getWeatherForLocation", userInfoTool)
                .description("获取用户的学习记录")
                .inputType(String.class)
                .build();
    }
}
