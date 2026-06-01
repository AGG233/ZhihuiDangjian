package com.rauio.smartdangjian.server.search.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.constants.RedisConstants;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.mapper.dto.HotCategoryRaw;
import com.rauio.smartdangjian.server.learning.mapper.dto.HotCourseRaw;
import com.rauio.smartdangjian.server.learning.mapper.dto.TrendRaw;
import com.rauio.smartdangjian.server.search.pojo.response.HotCategoryResponse;
import com.rauio.smartdangjian.server.search.pojo.response.HotCourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningHotspotService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final UserLearningRecordMapper userLearningRecordMapper;
    private final CourseMapper courseMapper;
    private final Clock clock;

    @Cacheable(value = RedisConstants.LEARNING_HOTSPOT_CACHE_PREFIX, key = "'courses:' + #limit", sync = true)
    public List<HotCourseResponse> getHotCourses(int limit) {
        int clampedLimit = clampLimit(limit);
        List<HotCourseRaw> rawList = userLearningRecordMapper.selectHotCourses(clampedLimit);
        if (rawList.isEmpty()) return Collections.emptyList();
        Set<Long> courseIds = rawList.stream().map(HotCourseRaw::getCourseId).collect(Collectors.toSet());
        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c, (a, b) -> a));
        return rawList.stream()
                .map(raw -> {
                    Course course = courseMap.get(raw.getCourseId());
                    return HotCourseResponse.builder()
                            .courseId(raw.getCourseId())
                            .title(course != null ? course.getTitle() : raw.getCourseTitle())
                            .learnerCount(raw.getLearnerCount())
                            .coverImageId(course != null ? course.getCoverImageId() : null)
                            .enrollmentCount(course != null ? course.getEnrollmentCount() : null)
                            .averageRating(course != null ? course.getAverageRating() : null)
                            .build();
                })
                .toList();
    }

    @Cacheable(value = RedisConstants.LEARNING_HOTSPOT_CACHE_PREFIX, key = "'categories:' + #limit", sync = true)
    public List<HotCategoryResponse> getHotCategories(int limit) {
        int clampedLimit = clampLimit(limit);
        List<HotCategoryRaw> rawList = userLearningRecordMapper.selectHotCategories(clampedLimit);
        if (rawList.isEmpty()) return Collections.emptyList();
        return rawList.stream()
                .map(raw -> HotCategoryResponse.builder()
                        .categoryId(raw.getCategoryId())
                        .name(raw.getCategoryName())
                        .learnerCount(raw.getLearnerCount())
                        .build())
                .toList();
    }

    @Cacheable(value = RedisConstants.LEARNING_HOTSPOT_CACHE_PREFIX, key = "'trends:' + #days", sync = true)
    public LearningTrendResponse getTrends(int days) {
        LocalDateTime since = LocalDateTime.now(clock).minusDays(days);
        List<TrendRaw> rawList = userLearningRecordMapper.selectDailyTrend(since);
        Map<String, Integer> dateCountMap =
                rawList.stream().collect(Collectors.toMap(TrendRaw::getDate, TrendRaw::getCount));
        int totalCount = rawList.stream().mapToInt(TrendRaw::getCount).sum();
        List<LearningTrendResponse.DailyCount> dailyData = new ArrayList<>();
        LocalDate today = LocalDate.now(clock);
        for (int i = days - 1; i >= 0; i--) {
            String dateStr = today.minusDays(i).toString();
            int count = dateCountMap.getOrDefault(dateStr, 0);
            dailyData.add(LearningTrendResponse.DailyCount.builder()
                    .date(dateStr)
                    .count(count)
                    .build());
        }
        double avgDailyCount = days > 0 ? (double) totalCount / days : 0;
        return LearningTrendResponse.builder()
                .days(days)
                .totalCount(totalCount)
                .avgDailyCount(avgDailyCount)
                .dailyData(dailyData)
                .build();
    }

    private int clampLimit(int limit) {
        if (limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }
}
