package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserQuizAnswerTool {

    private final UserQuizAnswerService userQuizAnswerService;
    private final UserService userService;

    @Tool(description = "获取当前用户最近的答题记录")
    public List<UserQuizAnswer> getRecentQuizAnswers(
            @ToolParam(description = "返回记录条数，默认10条") Integer limit,
            ToolContext toolContext
    ) {
        int safeLimit = limit == null || limit <= 0 ? 10 : limit;
        return userQuizAnswerService.getByUserId(ToolContextUtil.getUserId(toolContext, userService)).stream()
                .sorted(Comparator.comparing(UserQuizAnswer::getAnswerTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeLimit)
                .toList();
    }

    @Tool(description = "获取当前用户在指定测验下的答题记录")
    public List<UserQuizAnswer> getQuizAnswersByQuizId(
            @ToolParam(description = "测验ID") String quizId,
            ToolContext toolContext
    ) {
        return userQuizAnswerService.getByUserIdAndQuizId(ToolContextUtil.getUserId(toolContext, userService), quizId);
    }
}
