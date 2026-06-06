package com.rauio.smartdangjian.server.learning.api;

import java.time.LocalDateTime;
import java.util.List;

import com.rauio.smartdangjian.server.learning.pojo.dto.HotCategorySummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.HotCourseSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.LearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.LearningRecordSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.TrendSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserBehaviorDto;

/**
 * 学习记录查询门面 —— 供搜索/AI 模块等业务方调用的稳定接口。
 */
public interface LearningQueryFacade {

    List<LearningRecordSummaryDto> listRecordSummariesByUserId(Long userId);

    List<LearningRecordSummaryDto> listChapterRecordSummariesByUserIds(List<Long> userIds);

    List<UserBehaviorDto> listAllUserBehaviors();

    List<HotCourseSummaryDto> getHotCourses(int limit);

    List<HotCategorySummaryDto> getHotCategories(int limit);

    List<TrendSummaryDto> getDailyTrend(LocalDateTime since);

    /**
     * 查询用户最近 N 天的学习记录。
     *
     * @param userId     用户 ID
     * @param recentDays 最近天数
     * @return 学习记录 DTO 列表
     */
    List<LearningRecordDto> getRecentLearningRecords(Long userId, Integer recentDays);

    /**
     * 查询用户在指定课程下的学习记录。
     *
     * @param userId   用户 ID
     * @param courseId 课程 ID
     * @return 学习记录 DTO 列表
     */
    List<LearningRecordDto> getByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * 查询用户在指定课程章节下的学习记录。
     *
     * @param userId    用户 ID
     * @param courseId  课程 ID
     * @param chapterId 章节 ID
     * @return 学习记录 DTO 列表
     */
    List<LearningRecordDto> getByUserIdAndCourseIdAndChapterId(Long userId, Long courseId, Long chapterId);
}
