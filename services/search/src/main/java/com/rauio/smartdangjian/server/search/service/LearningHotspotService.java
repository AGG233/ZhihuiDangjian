package com.rauio.smartdangjian.server.search.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.course.service.course.CourseService;
import com.rauio.smartdangjian.server.learning.pojo.dto.HotCategorySummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.HotCourseSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.TrendSummaryDto;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
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

    private final UserLearningRecordService userLearningRecordService;
    private final CourseService courseService;
    private final Clock clock;

    public List<HotCourseResponse> getHotCourses(int limit) {
        int clampedLimit = clampLimit(limit);
        List<HotCourseSummaryDto> rawList = userLearningRecordService.getHotCourses(clampedLimit);
        if (rawList.isEmpty()) return Collections.emptyList();
        Set<Long> courseIds =
                rawList.stream().map(HotCourseSummaryDto::courseId).collect(Collectors.toSet());
        Map<Long, CourseResponse> courseMap = courseService.getCourseResponseMapByIds(courseIds);
        return rawList.stream()
                .map(raw -> {
                    CourseResponse course = courseMap.get(raw.courseId());
                    return HotCourseResponse.builder()
                            .courseId(raw.courseId())
                            .title(course != null ? course.getTitle() : raw.courseTitle())
                            .learnerCount(raw.learnerCount())
                            .coverImageId(course != null ? course.getCoverImageId() : null)
                            .enrollmentCount(course != null ? course.getEnrollmentCount() : null)
                            .averageRating(course != null ? course.getAverageRating() : null)
                            .build();
                })
                .toList();
    }

    public List<HotCategoryResponse> getHotCategories(int limit) {
        int clampedLimit = clampLimit(limit);
        List<HotCategorySummaryDto> rawList = userLearningRecordService.getHotCategories(clampedLimit);
        if (rawList.isEmpty()) return Collections.emptyList();
        return rawList.stream()
                .map(raw -> HotCategoryResponse.builder()
                        .categoryId(raw.categoryId())
                        .name(raw.categoryName())
                        .learnerCount(raw.learnerCount())
                        .build())
                .toList();
    }

    public LearningTrendResponse getTrends(int days) {
        LocalDateTime since = LocalDateTime.now(clock).minusDays(days);
        List<TrendSummaryDto> rawList = userLearningRecordService.getDailyTrend(since);
        Map<String, Integer> dateCountMap =
                rawList.stream().collect(Collectors.toMap(TrendSummaryDto::date, TrendSummaryDto::count));
        int totalCount = rawList.stream().mapToInt(TrendSummaryDto::count).sum();
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
