package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LearningTool {

    private final UserLearningRecordService userLearningRecordService;
    private final UserService userService;

    @Tool(description = "获取用户最近N天的学习记录")
    public List<UserLearningRecord> getRecentLearningRecord(
            @ToolParam(description = "最近几天，默认 7 天") Integer recentDays,
            ToolContext  toolContext
    ) {
        return userLearningRecordService.getRecentByUserId(ToolContextUtil.getUserId(toolContext, userService), recentDays);
    }

    @Tool(description = "获取用户某一课程的学习记录")
    public List<UserLearningRecord> getLearningRecordOfCourse(
            @ToolParam(description = "课程 ID") String courseId,
            ToolContext toolContext
    ) {
        return userLearningRecordService.getByUserIdAndCourseId(ToolContextUtil.getUserId(toolContext, userService), courseId);
    }

    @Tool(description = "获取用户某一课程的章节学习情况")
    public List<UserLearningRecord> getLearningRecordOfCourseChapter(
            @ToolParam(description = "课程 ID") String courseId,
            @ToolParam(description = "章节 ID") String chapterId,
            ToolContext toolContext
    ) {
        return userLearningRecordService.getByUserIdAndCourseIdAndChapterId(
                ToolContextUtil.getUserId(toolContext, userService),
                courseId,
                chapterId
        );
    }
}
