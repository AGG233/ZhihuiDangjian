package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.rauio.smartdangjian.constants.RedisConstants;
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
import com.rauio.smartdangjian.server.search.pojo.response.HotCourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;

/**
 * 缓存命中测试：第二次请求命中 @Cacheable 缓存不重算，缓存 key 可断言；缓存清空后数据更新可见。
 * 使用 ConcurrentMapCacheManager 代替 Redis，验证缓存语义而不依赖真实 Redis。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HotSpotServiceCacheTest.CacheConfig.class)
@DisplayName("HotSpotService 缓存命中")
class HotSpotServiceCacheTest {

    @Configuration
    @EnableCaching
    static class CacheConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    RedisConstants.HOT_COURSE_CACHE_PREFIX,
                    RedisConstants.HOT_CATEGORY_CACHE_PREFIX,
                    RedisConstants.LEARNING_TREND_CACHE_PREFIX);
        }

        @Bean
        CourseMapper courseMapper() {
            return mock(CourseMapper.class);
        }

        @Bean
        ChapterMapper chapterMapper() {
            return mock(ChapterMapper.class);
        }

        @Bean
        CategoryCourseMapper categoryCourseMapper() {
            return mock(CategoryCourseMapper.class);
        }

        @Bean
        CategoryMapper categoryMapper() {
            return mock(CategoryMapper.class);
        }

        @Bean
        UserLearningRecordMapper learningRecordMapper() {
            return mock(UserLearningRecordMapper.class);
        }

        @Bean
        HotSpotService hotSpotService(
                CourseMapper courseMapper,
                ChapterMapper chapterMapper,
                CategoryCourseMapper categoryCourseMapper,
                CategoryMapper categoryMapper,
                UserLearningRecordMapper learningRecordMapper) {
            return new HotSpotService(
                    courseMapper, chapterMapper, categoryCourseMapper, categoryMapper, learningRecordMapper);
        }
    }

    @Autowired
    private HotSpotService hotSpotService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserLearningRecordMapper learningRecordMapper;

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, Course.class);
        TableInfoHelper.initTableInfo(assistant, CategoryCourse.class);
        TableInfoHelper.initTableInfo(assistant, Chapter.class);
        TableInfoHelper.initTableInfo(assistant, Category.class);
        TableInfoHelper.initTableInfo(assistant, UserLearningRecord.class);
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(courseMapper, learningRecordMapper);
        when(learningRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
    }

    @Test
    @DisplayName("热门课程第二次请求命中缓存，缓存 key 可断言，清空后重新计算")
    void hotCoursesCacheHitAndRefreshAfterClear() {
        Course c1 = Course.builder().id(1L).title("课程A").enrollmentCount(100).build();
        when(courseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c1));

        List<HotCourseResponse> first = hotSpotService.getHotCourses(5);
        assertThat(first).hasSize(1);
        verify(courseMapper, times(1)).selectList(any(LambdaQueryWrapper.class));

        // 数据源变更后再次请求 → 命中缓存，返回旧数据且不重新查询
        Course c2 = Course.builder().id(2L).title("课程B").enrollmentCount(999).build();
        when(courseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c2));

        List<HotCourseResponse> second = hotSpotService.getHotCourses(5);
        assertThat(second).hasSize(1);
        assertThat(second.get(0).getCourseId()).isEqualTo(1L);
        verify(courseMapper, times(1)).selectList(any(LambdaQueryWrapper.class));

        // 缓存 key 断言：topN=5 已写入缓存
        Cache cache = cacheManager.getCache(RedisConstants.HOT_COURSE_CACHE_PREFIX);
        assertThat(cache).isNotNull();
        assertThat(cache.get(5)).isNotNull();

        // 清空缓存（模拟过期）→ 重新计算，数据更新可见
        cache.clear();
        List<HotCourseResponse> third = hotSpotService.getHotCourses(5);
        assertThat(third).hasSize(1);
        assertThat(third.get(0).getCourseId()).isEqualTo(2L);
        verify(courseMapper, times(2)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("学习趋势第二次请求命中缓存不重算")
    void learningTrendCacheHit() {
        List<LearningTrendResponse> first = hotSpotService.getLearningTrend(30);
        assertThat(first).isEmpty();
        verify(learningRecordMapper, times(1)).selectList(any(LambdaQueryWrapper.class));

        // 第二次请求 → 缓存命中，不重新查询
        assertThat(hotSpotService.getLearningTrend(30)).isEmpty();
        verify(learningRecordMapper, times(1)).selectList(any(LambdaQueryWrapper.class));

        Cache cache = cacheManager.getCache(RedisConstants.LEARNING_TREND_CACHE_PREFIX);
        assertThat(cache).isNotNull();
        assertThat(cache.get(30)).isNotNull();
    }

    @Test
    @DisplayName("不同参数使用不同缓存 key")
    void cacheKeyDiffersByParameter() {
        when(courseMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(
                        Course.builder().id(1L).title("课程").enrollmentCount(10).build()));
        hotSpotService.getHotCourses(5);
        hotSpotService.getHotCourses(10);

        Cache cache = cacheManager.getCache(RedisConstants.HOT_COURSE_CACHE_PREFIX);
        assertThat(cache.get(5)).isNotNull();
        assertThat(cache.get(10)).isNotNull();
        // 两次调用各生成独立列表实例
        assertThat(cache.get(5).get()).isNotSameAs(cache.get(10).get());
    }
}
