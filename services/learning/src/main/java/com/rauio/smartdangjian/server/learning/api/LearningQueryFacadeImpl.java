package com.rauio.smartdangjian.server.learning.api;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.server.learning.pojo.dto.HotCategorySummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.HotCourseSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.LearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.LearningRecordSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.TrendSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserBehaviorDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LearningQueryFacadeImpl implements LearningQueryFacade {

    private final UserLearningRecordService userLearningRecordService;

    @Override
    public List<LearningRecordSummaryDto> listRecordSummariesByUserId(Long userId) {
        return userLearningRecordService.listRecordSummariesByUserId(userId);
    }

    @Override
    public List<LearningRecordSummaryDto> listChapterRecordSummariesByUserIds(List<Long> userIds) {
        return userLearningRecordService.listChapterRecordSummariesByUserIds(userIds);
    }

    @Override
    public List<UserBehaviorDto> listAllUserBehaviors() {
        return userLearningRecordService.listAllUserBehaviors();
    }

    @Override
    public List<HotCourseSummaryDto> getHotCourses(int limit) {
        return userLearningRecordService.getHotCourses(limit);
    }

    @Override
    public List<HotCategorySummaryDto> getHotCategories(int limit) {
        return userLearningRecordService.getHotCategories(limit);
    }

    @Override
    public List<TrendSummaryDto> getDailyTrend(LocalDateTime since) {
        return userLearningRecordService.getDailyTrend(since);
    }

    @Override
    public List<LearningRecordDto> getRecentLearningRecords(Long userId, Integer recentDays) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return toLearningRecordDtoList(userLearningRecordService.getRecentByUserId(Long.toString(userId), recentDays));
    }

    @Override
    public List<LearningRecordDto> getByUserIdAndCourseId(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            return Collections.emptyList();
        }
        return toLearningRecordDtoList(userLearningRecordService.getByUserIdAndCourseId(userId, courseId));
    }

    @Override
    public List<LearningRecordDto> getByUserIdAndCourseIdAndChapterId(Long userId, Long courseId, Long chapterId) {
        if (userId == null || courseId == null || chapterId == null) {
            return Collections.emptyList();
        }
        return toLearningRecordDtoList(
                userLearningRecordService.getByUserIdAndCourseIdAndChapterId(userId, courseId, chapterId));
    }

    private List<LearningRecordDto> toLearningRecordDtoList(List<UserLearningRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .filter(Objects::nonNull)
                .map(this::toLearningRecordDto)
                .collect(Collectors.toList());
    }

    private LearningRecordDto toLearningRecordDto(UserLearningRecord record) {
        return LearningRecordDto.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .chapterId(record.getChapterId())
                .duration(record.getDuration())
                .deviceType(record.getDeviceType())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
