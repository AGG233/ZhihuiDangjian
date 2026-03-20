package com.rauio.smartdangjian.service.ai.tool;

import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.pojo.vo.UserLearningRecordVO;
import com.rauio.smartdangjian.pojo.vo.UserVO;
import com.rauio.smartdangjian.service.learning.UserLearningRecordService;
import com.rauio.smartdangjian.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoTool {

    private final UserService userService;
    private final UserLearningRecordService userLearningRecordService;

    @Tool(name = "getCurrentUserInfo", description = "获取当前登录用户的身份、姓名、角色等基本信息。当你不知道当前用户是谁，或者需要确认用户身份时，必须首先调用此工具。")
    public UserVO getCurrentUserInfo(ToolContext toolContext) {
        UserVO currentUser = (UserVO) toolContext.getContext().get("CURRENT_USER");
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        return currentUser;
    }

    @Tool(name = "getRecentLearningRecords", description = "获取当前登录用户在系统中的最近学习历史记录。如果用户询问'我最近学了什么'或'查看我的学习轨迹'，请调用此工具。")
    public List<UserLearningRecordVO> getRecentLearningRecords(
            @ToolParam(description = "需要获取的学习记录条数，默认10") Integer n
    ) {
        int limit = (n == null || n <= 0) ? 10 : n;
        List<UserLearningRecordVO> records = userLearningRecordService.getByUserId(userService.getUserIDFromAuthentication());
        records.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return records.size() > limit ? records.subList(0, limit) : records;
    }

    @Tool(name = "getLearnedCourseIds", description = "获取当前登录用户已学习过的课程ID列表")
    public List<String> getLearnedCourseIds() {
        return userLearningRecordService.selectLearnedCoursesByUserId(userService.getUserIDFromAuthentication());
    }
}
