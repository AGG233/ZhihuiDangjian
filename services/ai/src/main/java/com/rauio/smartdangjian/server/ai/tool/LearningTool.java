package com.rauio.smartdangjian.server.ai.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.learning.api.LearningQueryFacade;
import com.rauio.smartdangjian.server.learning.pojo.dto.LearningRecordDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LearningTool {

    private final LearningQueryFacade learningQueryFacade;
    private final CurrentUserProvider currentUserProvider;

    @Tool(description = "获取用户最近N天的学习记录")
    public List<Map<String, Object>> getRecentLearningRecord(
            @ToolParam(description = "最近几天，默认 7 天") Integer recentDays) {
        List<LearningRecordDto> records = learningQueryFacade.getRecentLearningRecords(
                IdUtil.parseNullable(ToolContextUtil.resolveUserId(currentUserProvider)), recentDays);
        return records.stream().map(this::toMap).toList();
    }

    @Tool(description = "获取用户某一课程的学习记录")
    public List<Map<String, Object>> getLearningRecordOfCourse(@ToolParam(description = "课程 ID") String courseId) {
        List<LearningRecordDto> records = learningQueryFacade.getByUserIdAndCourseId(
                IdUtil.parseNullable(ToolContextUtil.resolveUserId(currentUserProvider)), IdUtil.parse(courseId));
        return records.stream().map(this::toMap).toList();
    }

    @Tool(description = "获取用户某一课程的章节学习情况")
    public List<Map<String, Object>> getLearningRecordOfCourseChapter(
            @ToolParam(description = "课程 ID") String courseId, @ToolParam(description = "章节 ID") String chapterId) {
        List<LearningRecordDto> records = learningQueryFacade.getByUserIdAndCourseIdAndChapterId(
                IdUtil.parseNullable(ToolContextUtil.resolveUserId(currentUserProvider)),
                IdUtil.parse(courseId),
                IdUtil.parse(chapterId));
        return records.stream().map(this::toMap).toList();
    }

    private Map<String, Object> toMap(LearningRecordDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("userId", dto.getUserId());
        map.put("chapterId", dto.getChapterId());
        map.put("duration", dto.getDuration());
        map.put("createdAt", dto.getCreatedAt());
        return map;
    }
}
