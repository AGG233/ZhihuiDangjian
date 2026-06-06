package com.rauio.smartdangjian.server.ai.tool;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.quiz.api.UserQuizQueryFacade;
import com.rauio.smartdangjian.server.quiz.pojo.dto.UserQuizAnswerDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserQuizAnswerTool {

    private final CurrentUserProvider currentUserProvider;
    private final UserQuizQueryFacade userQuizQueryFacade;

    @Tool(description = "获取当前用户最近的答题记录")
    public List<UserQuizAnswerDto> getRecentQuizAnswers(@ToolParam(description = "返回记录条数，默认10条") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : limit;
        Long userId = IdUtil.parseNullable(currentUserProvider.getCurrentUserId());
        if (userId == null) {
            return List.of();
        }
        return userQuizQueryFacade.listByUserId(userId).stream()
                .sorted((a, b) -> b.getAnswerTime().compareTo(a.getAnswerTime()))
                .limit(safeLimit)
                .toList();
    }

    @Tool(description = "获取当前用户在指定测验下的答题记录")
    public List<UserQuizAnswerDto> getQuizAnswersByQuizId(@ToolParam(description = "测验ID") String quizId) {
        Long userId = IdUtil.parseNullable(currentUserProvider.getCurrentUserId());
        return userQuizQueryFacade.listByUserIdAndQuizId(userId, IdUtil.parse(quizId));
    }
}
