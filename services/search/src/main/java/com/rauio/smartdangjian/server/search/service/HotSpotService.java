package com.rauio.smartdangjian.server.search.service;

import static com.rauio.smartdangjian.constants.RedisConstants.HOT_CATEGORY_CACHE_PREFIX;
import static com.rauio.smartdangjian.constants.RedisConstants.HOT_COURSE_CACHE_PREFIX;
import static com.rauio.smartdangjian.constants.RedisConstants.LEARNING_TREND_CACHE_PREFIX;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.search.pojo.response.HotCategoryResponse;
import com.rauio.smartdangjian.server.search.pojo.response.HotCourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotSpotService {

    /** 热门课程/分类默认 Top N */
    private static final int DEFAULT_TOP_N = 10;

    /** 热门课程近30天学习人数统计窗口（天） */
    private static final int RECENT_LEARNER_WINDOW_DAYS = 30;

    /** 学习趋势默认统计天数 */
    private static final int DEFAULT_TREND_DAYS = 30;

    /** 学习趋势最大统计天数（防止超大查询） */
    private static final int MAX_TREND_DAYS = 365;

    private final CourseMapper courseMapper;
    private final ChapterMapper chapterMapper;
    private final CategoryCourseMapper categoryCourseMapper;
    private final CategoryMapper categoryMapper;
    private final UserLearningRecordMapper learningRecordMapper;

    /**
     * 热门课程 Top N。
     * <p>
     * 热度口径（固化）：hotScore = enrollmentCount + 近30天学习人数。近30天学习人数 = 近30天
     * user_learning_record 按章节关联课程后的去重用户数；无学习记录时退化为纯 enrollmentCount 排序。
     * 空数据返回空列表。
     */
    @Cacheable(value = HOT_COURSE_CACHE_PREFIX, key = "#topN")
    public List<HotCourseResponse> getHotCourses(int topN) {
        int limit = topN <= 0 ? DEFAULT_TOP_N : topN;
        List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getIsPublished, true)
                .select(Course::getId, Course::getTitle, Course::getEnrollmentCount));
        if (courses == null || courses.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Integer> recentLearnerCounts = countRecentLearnersByCourse();

        return courses.stream()
                .map(course -> {
                    int enrollment = course.getEnrollmentCount() != null ? course.getEnrollmentCount() : 0;
                    int recentLearners = recentLearnerCounts.getOrDefault(course.getId(), 0);
                    return HotCourseResponse.builder()
                            .courseId(course.getId())
                            .title(course.getTitle())
                            .enrollmentCount(enrollment)
                            .recentLearnerCount(recentLearners)
                            .hotScore(enrollment + recentLearners)
                            .build();
                })
                .sorted(Comparator.comparing(HotCourseResponse::getHotScore).reversed())
                .limit(limit)
                .toList();
    }

    /**
     * 热门分类 Top N。
     * <p>
     * 热度口径（固化）：按分类下所有已发布课程的 enrollmentCount 汇总，关联课程数 courseCount 一并返回；
     * 未发布课程不计入。空数据返回空列表。
     */
    @Cacheable(value = HOT_CATEGORY_CACHE_PREFIX, key = "#topN")
    public List<HotCategoryResponse> getHotCategories(int topN) {
        int limit = topN <= 0 ? DEFAULT_TOP_N : topN;

        List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getIsPublished, true)
                .select(Course::getId, Course::getEnrollmentCount));
        if (courses == null || courses.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Integer> courseEnrollment = courses.stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(
                        Course::getId, c -> c.getEnrollmentCount() != null ? c.getEnrollmentCount() : 0, (a, b) -> a));

        List<CategoryCourse> relations = categoryCourseMapper.selectList(new LambdaQueryWrapper<CategoryCourse>()
                .select(CategoryCourse::getCategoryId, CategoryCourse::getCourseId));
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, CategoryAgg> aggMap = new HashMap<>();
        for (CategoryCourse relation : relations) {
            Long categoryId = relation.getCategoryId();
            Integer enrollment = courseEnrollment.get(relation.getCourseId());
            if (categoryId == null || enrollment == null) {
                continue;
            }
            aggMap.computeIfAbsent(categoryId, k -> new CategoryAgg()).add(enrollment);
        }
        if (aggMap.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> categoryNames = loadCategoryNames(aggMap.keySet());

        return aggMap.entrySet().stream()
                .map(entry -> HotCategoryResponse.builder()
                        .categoryId(entry.getKey())
                        .categoryName(categoryNames.get(entry.getKey()))
                        .courseCount(entry.getValue().courseCount)
                        .enrollmentSum(entry.getValue().enrollmentSum)
                        .build())
                .sorted(Comparator.comparing(HotCategoryResponse::getEnrollmentSum)
                        .reversed())
                .limit(limit)
                .toList();
    }

    /**
     * 学习趋势：按天返回学习人次与总时长。
     * <p>
     * 统计口径（固化）：取近 days 天（默认 30，夹取到 1~365）的 user_learning_record，按 start_time
     * 所在自然日分组：learningCount = 当日记录条数（人次），totalDuration = 当日 duration 合计（null 按 0 计，秒）。
     * 按日期升序返回；无记录返回空列表。
     */
    @Cacheable(value = LEARNING_TREND_CACHE_PREFIX, key = "#days")
    public List<LearningTrendResponse> getLearningTrend(int days) {
        int safeDays = days <= 0 ? DEFAULT_TREND_DAYS : Math.min(days, MAX_TREND_DAYS);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(safeDays);

        List<UserLearningRecord> records = learningRecordMapper.selectList(new LambdaQueryWrapper<UserLearningRecord>()
                .ge(UserLearningRecord::getStartTime, cutoff)
                .select(UserLearningRecord::getStartTime, UserLearningRecord::getDuration));
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        Map<LocalDate, TrendAgg> trendMap = new TreeMap<>();
        for (UserLearningRecord record : records) {
            if (record.getStartTime() == null) {
                continue;
            }
            trendMap.computeIfAbsent(record.getStartTime().toLocalDate(), k -> new TrendAgg())
                    .add(record.getDuration());
        }

        return trendMap.entrySet().stream()
                .map(entry -> LearningTrendResponse.builder()
                        .date(entry.getKey())
                        .learningCount(entry.getValue().learningCount)
                        .totalDuration(entry.getValue().totalDuration)
                        .build())
                .toList();
    }

    private Map<Long, Integer> countRecentLearnersByCourse() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RECENT_LEARNER_WINDOW_DAYS);
        List<UserLearningRecord> records = learningRecordMapper.selectList(new LambdaQueryWrapper<UserLearningRecord>()
                .ge(UserLearningRecord::getStartTime, cutoff)
                .select(UserLearningRecord::getUserId, UserLearningRecord::getChapterId));
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> chapterIds = records.stream()
                .map(UserLearningRecord::getChapterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (chapterIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Long> chapterCourseMap = chapterMapper
                .selectList(new LambdaQueryWrapper<Chapter>()
                        .in(Chapter::getId, chapterIds)
                        .select(Chapter::getId, Chapter::getCourseId))
                .stream()
                .filter(ch -> ch.getCourseId() != null)
                .collect(Collectors.toMap(Chapter::getId, Chapter::getCourseId, (left, right) -> left));

        Map<Long, Set<Long>> courseUsers = new HashMap<>();
        for (UserLearningRecord record : records) {
            Long courseId = chapterCourseMap.get(record.getChapterId());
            if (courseId == null || record.getUserId() == null) {
                continue;
            }
            courseUsers.computeIfAbsent(courseId, k -> new HashSet<>()).add(record.getUserId());
        }

        Map<Long, Integer> result = new HashMap<>();
        courseUsers.forEach((courseId, users) -> result.put(courseId, users.size()));
        return result;
    }

    private Map<Long, String> loadCategoryNames(Set<Long> categoryIds) {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .in(Category::getId, categoryIds)
                .select(Category::getId, Category::getName));
        return categories.stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    private static final class CategoryAgg {
        private int courseCount;
        private int enrollmentSum;

        private void add(int enrollment) {
            courseCount++;
            enrollmentSum += enrollment;
        }
    }

    private static final class TrendAgg {
        private int learningCount;
        private int totalDuration;

        private void add(Integer duration) {
            learningCount++;
            totalDuration += duration != null ? duration : 0;
        }
    }
}
