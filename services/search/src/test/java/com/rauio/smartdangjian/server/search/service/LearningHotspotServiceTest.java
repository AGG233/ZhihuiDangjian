package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;

import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.mapper.dto.HotCategoryRaw;
import com.rauio.smartdangjian.server.learning.mapper.dto.HotCourseRaw;
import com.rauio.smartdangjian.server.learning.mapper.dto.TrendRaw;
import com.rauio.smartdangjian.server.search.pojo.response.HotCategoryResponse;
import com.rauio.smartdangjian.server.search.pojo.response.HotCourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("LearningHotspotService 学习热点统计")
class LearningHotspotServiceTest {

    @Mock
    private UserLearningRecordMapper userLearningRecordMapper;

    @Mock
    private CourseMapper courseMapper;

    private LearningHotspotService learningHotspotService;

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-31T10:15:30Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        learningHotspotService = new LearningHotspotService(userLearningRecordMapper, courseMapper, FIXED_CLOCK);
    }

    @Test
    @DisplayName("缓存入口启用 sync，避免热点 key 并发击穿")
    void cacheableMethodsUseSync() throws NoSuchMethodException {
        assertCacheableSync("getHotCourses", int.class);
        assertCacheableSync("getHotCategories", int.class);
        assertCacheableSync("getTrends", int.class);
    }

    private void assertCacheableSync(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = LearningHotspotService.class.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(Cacheable.class).sync()).isTrue();
    }

    // ==================== getHotCourses ====================

    @Test
    @DisplayName("正常返回热门课程列表")
    void getHotCoursesReturnsEnrichedList() {
        HotCourseRaw raw1 = new HotCourseRaw();
        raw1.setCourseId(1L);
        raw1.setCourseTitle("rawTitle1");
        raw1.setLearnerCount(100);

        HotCourseRaw raw2 = new HotCourseRaw();
        raw2.setCourseId(2L);
        raw2.setCourseTitle("rawTitle2");
        raw2.setLearnerCount(80);

        when(userLearningRecordMapper.selectHotCourses(10)).thenReturn(List.of(raw1, raw2));

        Course course1 = Course.builder()
                .id(1L)
                .title("课程1")
                .coverImageId(10L)
                .enrollmentCount(50)
                .averageRating(BigDecimal.valueOf(4.5))
                .build();
        Course course2 = Course.builder()
                .id(2L)
                .title("课程2")
                .coverImageId(20L)
                .enrollmentCount(30)
                .averageRating(BigDecimal.valueOf(4.0))
                .build();
        when(courseMapper.selectBatchIds(any())).thenReturn(List.of(course1, course2));

        List<HotCourseResponse> result = learningHotspotService.getHotCourses(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCourseId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("课程1");
        assertThat(result.get(0).getLearnerCount()).isEqualTo(100);
        assertThat(result.get(0).getCoverImageId()).isEqualTo(10L);
        assertThat(result.get(0).getEnrollmentCount()).isEqualTo(50);
        assertThat(result.get(0).getAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(4.5));

        assertThat(result.get(1).getCourseId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("课程2");
    }

    @Test
    @DisplayName("空结果返回空列表")
    void getHotCoursesEmptyResultReturnsEmptyList() {
        when(userLearningRecordMapper.selectHotCourses(10)).thenReturn(Collections.emptyList());

        List<HotCourseResponse> result = learningHotspotService.getHotCourses(10);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("limit 为负数时使用默认值 10")
    void getHotCoursesNegativeLimitUsesDefault() {
        HotCourseRaw raw = new HotCourseRaw();
        raw.setCourseId(1L);
        raw.setCourseTitle("title");
        raw.setLearnerCount(10);

        when(userLearningRecordMapper.selectHotCourses(10)).thenReturn(List.of(raw));
        when(courseMapper.selectBatchIds(any()))
                .thenReturn(List.of(Course.builder().id(1L).title("课程").build()));

        List<HotCourseResponse> result = learningHotspotService.getHotCourses(-5);

        assertThat(result).hasSize(1);
        verify(userLearningRecordMapper).selectHotCourses(10);
    }

    @Test
    @DisplayName("limit 超过 50 被截断为 50")
    void getHotCoursesExcessiveLimitClampedToMax() {
        HotCourseRaw raw = new HotCourseRaw();
        raw.setCourseId(1L);
        raw.setCourseTitle("title");
        raw.setLearnerCount(10);

        when(userLearningRecordMapper.selectHotCourses(50)).thenReturn(List.of(raw));
        when(courseMapper.selectBatchIds(any()))
                .thenReturn(List.of(Course.builder().id(1L).title("课程").build()));

        List<HotCourseResponse> result = learningHotspotService.getHotCourses(100);

        assertThat(result).hasSize(1);
        verify(userLearningRecordMapper).selectHotCourses(50);
    }

    @Test
    @DisplayName("courseMapper 返回 null 时回退到 raw 数据")
    void getHotCoursesNullCourseFallsBackToRawData() {
        HotCourseRaw raw = new HotCourseRaw();
        raw.setCourseId(99L);
        raw.setCourseTitle("rawFallbackTitle");
        raw.setLearnerCount(42);

        when(userLearningRecordMapper.selectHotCourses(10)).thenReturn(List.of(raw));
        when(courseMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());

        List<HotCourseResponse> result = learningHotspotService.getHotCourses(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseId()).isEqualTo(99L);
        assertThat(result.get(0).getTitle()).isEqualTo("rawFallbackTitle");
        assertThat(result.get(0).getLearnerCount()).isEqualTo(42);
        assertThat(result.get(0).getCoverImageId()).isNull();
        assertThat(result.get(0).getEnrollmentCount()).isNull();
        assertThat(result.get(0).getAverageRating()).isNull();
    }

    // ==================== getHotCategories ====================

    @Test
    @DisplayName("正常返回热门分类")
    void getHotCategoriesReturnsList() {
        HotCategoryRaw raw1 = new HotCategoryRaw();
        raw1.setCategoryId(1L);
        raw1.setCategoryName("党史");
        raw1.setLearnerCount(200);

        HotCategoryRaw raw2 = new HotCategoryRaw();
        raw2.setCategoryId(2L);
        raw2.setCategoryName("理论");
        raw2.setLearnerCount(150);

        when(userLearningRecordMapper.selectHotCategories(10)).thenReturn(List.of(raw1, raw2));

        List<HotCategoryResponse> result = learningHotspotService.getHotCategories(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("党史");
        assertThat(result.get(0).getLearnerCount()).isEqualTo(200);
        assertThat(result.get(1).getCategoryId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("理论");
        assertThat(result.get(1).getLearnerCount()).isEqualTo(150);
    }

    @Test
    @DisplayName("空结果返回空列表")
    void getHotCategoriesEmptyResultReturnsEmptyList() {
        when(userLearningRecordMapper.selectHotCategories(10)).thenReturn(Collections.emptyList());

        List<HotCategoryResponse> result = learningHotspotService.getHotCategories(10);

        assertThat(result).isEmpty();
    }

    // ==================== getTrends ====================

    @Test
    @DisplayName("正常返回趋势数据")
    void getTrendsReturnsDailyData() {
        String today = LocalDate.now(FIXED_CLOCK).toString();
        String yesterday = LocalDate.now(FIXED_CLOCK).minusDays(1).toString();

        TrendRaw trend1 = new TrendRaw();
        trend1.setDate(yesterday);
        trend1.setCount(5);

        TrendRaw trend2 = new TrendRaw();
        trend2.setDate(today);
        trend2.setCount(3);

        when(userLearningRecordMapper.selectDailyTrend(any(LocalDateTime.class)))
                .thenReturn(List.of(trend1, trend2));

        LearningTrendResponse result = learningHotspotService.getTrends(2);

        assertThat(result.getDays()).isEqualTo(2);
        assertThat(result.getTotalCount()).isEqualTo(8);
        assertThat(result.getAvgDailyCount()).isEqualTo(4.0);
        assertThat(result.getDailyData()).hasSize(2);
        assertThat(result.getDailyData().get(0).getDate()).isEqualTo(yesterday);
        assertThat(result.getDailyData().get(0).getCount()).isEqualTo(5);
        assertThat(result.getDailyData().get(1).getDate()).isEqualTo(today);
        assertThat(result.getDailyData().get(1).getCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("缺失日期自动补 0")
    void getTrendsMissingDatesFilledWithZero() {
        String today = LocalDate.now(FIXED_CLOCK).toString();
        String yesterday = LocalDate.now(FIXED_CLOCK).minusDays(1).toString();
        String twoDaysAgo = LocalDate.now(FIXED_CLOCK).minusDays(2).toString();

        TrendRaw trend = new TrendRaw();
        trend.setDate(today);
        trend.setCount(3);

        when(userLearningRecordMapper.selectDailyTrend(any(LocalDateTime.class)))
                .thenReturn(List.of(trend));

        LearningTrendResponse result = learningHotspotService.getTrends(3);

        assertThat(result.getDailyData()).hasSize(3);
        assertThat(result.getDailyData().get(0).getDate()).isEqualTo(twoDaysAgo);
        assertThat(result.getDailyData().get(0).getCount()).isEqualTo(0);
        assertThat(result.getDailyData().get(1).getDate()).isEqualTo(yesterday);
        assertThat(result.getDailyData().get(1).getCount()).isEqualTo(0);
        assertThat(result.getDailyData().get(2).getDate()).isEqualTo(today);
        assertThat(result.getDailyData().get(2).getCount()).isEqualTo(3);
        assertThat(result.getTotalCount()).isEqualTo(3);
        assertThat(result.getAvgDailyCount()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("days=0 时 avgDailyCount=0")
    void getTrendsZeroDaysReturnsZeroAvg() {
        when(userLearningRecordMapper.selectDailyTrend(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        LearningTrendResponse result = learningHotspotService.getTrends(0);

        assertThat(result.getDays()).isEqualTo(0);
        assertThat(result.getTotalCount()).isEqualTo(0);
        assertThat(result.getAvgDailyCount()).isEqualTo(0.0);
        assertThat(result.getDailyData()).isEmpty();
    }
}
