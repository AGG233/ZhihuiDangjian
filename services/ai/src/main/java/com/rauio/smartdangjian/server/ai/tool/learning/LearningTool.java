package com.rauio.smartdangjian.server.ai.tool.learning;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.ai.tool.user.UserLearningRecordTool;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LearningTool {

    private final UserLearningRecordService  userLearningRecordService;
    private final UserService userService;


    @Tool(description = "获取用户的学习记录")
    public Page<UserLearningRecord> getUserLearningRecord(
            @ToolParam(description = "请求体") UserLearningRecordDto dto,
            @ToolParam(description = "页码")  Integer pageNum,
            @ToolParam(description = "每页包含的记录条数") Integer pageSize
    ) {
        return userLearningRecordService.getPage(dto,pageNum,pageSize);
    }

//    private final UserLearningRecordTool userLearningRecordTool;
//    private final LearnedCourseTool learnedCourseTool;
//    private final RecentLearningRecordsTool  recentLearningRecordsTool;
//
//
//    @Bean
//    public ToolCallback learningRecordCallBack() {
//        return FunctionToolCallback
//                .builder("getLearningRecord", userLearningRecordTool)
//                .description("获取用户的学习记录")
//                .inputType(UserLearningRecordDto.class)
//                .build();
//    }
//
//    @Bean
//    public ToolCallback learnedCourseCallBack() {
//        return FunctionToolCallback
//                .builder("getLearnedCourse", learnedCourseTool)
//                .description("获取用户已经学过的课程")
//                .inputType(UserLearningRecordDto.class)
//                .build();
//    }
//
//    @Bean
//    public ToolCallback recentLearningCallBack() {
//        return FunctionToolCallback
//                .builder("getRecentLearningRecords",recentLearningRecordsTool)
//                .description("获取当前登录用户在系统中的最近学习历史记录。如果用户询问'我最近学了什么'或'查看我的学习轨迹'，请调用此工具")
//                .inputType(UserLearningRecordDto.class)
//                .build();
//    }
}
