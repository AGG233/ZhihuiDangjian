package com.rauio.smartdangjian.server.quiz.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.rauio.smartdangjian.server.quiz.pojo.dto.UserQuizAnswerDto;
import com.rauio.smartdangjian.server.quiz.pojo.dto.UserQuizAnswerSummaryDto;

/**
 * 用户答题查询门面 —— 供搜索模块、AI 模块等业务方调用的稳定接口。
 */
public interface UserQuizQueryFacade {

    List<UserQuizAnswerSummaryDto> listAnswerSummariesByUserId(Long userId);

    Map<Long, String> getDifficultyMapByIds(Collection<Long> quizIds);

    /**
     * 查询用户的全部答题记录。
     *
     * @param userId 用户 ID
     * @return 答题记录列表（不含实体层信息）
     */
    List<UserQuizAnswerDto> listByUserId(Long userId);

    /**
     * 查询用户在指定测验下的答题记录。
     *
     * @param userId 用户 ID
     * @param quizId 测验 ID
     * @return 答题记录列表（不含实体层信息）
     */
    List<UserQuizAnswerDto> listByUserIdAndQuizId(Long userId, Long quizId);
}
