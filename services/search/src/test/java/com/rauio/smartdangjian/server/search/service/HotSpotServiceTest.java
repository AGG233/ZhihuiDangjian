package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HotSpotService 学习热点统计")
class HotSpotServiceTest {

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

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private ChapterMapper chapterMapper;

    @Mock
    private CategoryCourseMapper categoryCourseMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private UserLearningRecordMapper learningRecordMapper;

    @InjectMocks
    private HotSpotService hotSpotService;

    @BeforeEach
    void setUp() {
        // 默认无学习记录，避免影响纯 enrollment 排序用例
        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
    }

    // ==================== getHotCourses ====================

    @Test
    @DisplayName("按 enrollmentCount 与近30天学习人数加权排序")
    void hotCoursesWeightedByEnrollmentAndRecentLearners() {
        Course c1 = Course.builder().id(1L).title("课程A").enrollmentCount(100).build();
        Course c2 = Course.builder().id(2L).title("课程B").enrollmentCount(200).build();
        doReturn(List.of(c1, c2)).when(courseMapper).selectList(any(LambdaQueryWrapper.class));

        // 课程A 近30天有 3 人学习 → hotScore = 103；课程B 无学习 → 200
        UserLearningRecord r1 =
                UserLearningRecord.builder().userId(1L).chapterId(10L).build();
        UserLearningRecord r2 =
                UserLearningRecord.builder().userId(2L).chapterId(10L).build();
        UserLearningRecord r3 =
                UserLearningRecord.builder().userId(3L).chapterId(10L).build();
        doReturn(List.of(r1, r2, r3)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(List.of(Chapter.builder().id(10L).courseId(1L).build()))
                .when(chapterMapper)
                .selectList(any(LambdaQueryWrapper.class));

        List<HotCourseResponse> result = hotSpotService.getHotCourses(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCourseId()).isEqualTo(2L);
        assertThat(result.get(0).getHotScore()).isEqualTo(200);
        assertThat(result.get(1).getCourseId()).isEqualTo(1L);
        assertThat(result.get(1).getHotScore()).isEqualTo(103);
        assertThat(result.get(1).getRecentLearnerCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("近30天学习人数按用户去重统计")
    void hotCoursesRecentLearnersDeduplicatedByUser() {
        Course c1 = Course.builder().id(1L).title("课程A").enrollmentCount(100).build();
        doReturn(List.of(c1)).when(courseMapper).selectList(any(LambdaQueryWrapper.class));

        // 同一用户学习两次 → 只计 1 人
        UserLearningRecord r1 =
                UserLearningRecord.builder().userId(1L).chapterId(10L).build();
        UserLearningRecord r2 =
                UserLearningRecord.builder().userId(1L).chapterId(11L).build();
        doReturn(List.of(r1, r2)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(List.of(
                        Chapter.builder().id(10L).courseId(1L).build(),
                        Chapter.builder().id(11L).courseId(1L).build()))
                .when(chapterMapper)
                .selectList(any(LambdaQueryWrapper.class));

        List<HotCourseResponse> result = hotSpotService.getHotCourses(10);

        assertThat(result.get(0).getRecentLearnerCount()).isEqualTo(1);
        assertThat(result.get(0).getHotScore()).isEqualTo(101);
    }

    @Test
    @DisplayName("无学习记录时退化为纯 enrollmentCount 排序")
    void hotCoursesFallbackToEnrollmentOnly() {
        Course c1 = Course.builder().id(1L).title("课程A").enrollmentCount(100).build();
        Course c2 = Course.builder().id(2L).title("课程B").enrollmentCount(300).build();
        doReturn(List.of(c1, c2)).when(courseMapper).selectList(any(LambdaQueryWrapper.class));

        List<HotCourseResponse> result = hotSpotService.getHotCourses(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCourseId()).isEqualTo(2L);
        assertThat(result.get(1).getCourseId()).isEqualTo(1L);
        assertThat(result.get(0).getRecentLearnerCount()).isZero();
    }

    @Test
    @DisplayName("课程列表为空时返回空列表")
    void hotCoursesEmptyCoursesReturnsEmptyList() {
        doReturn(Collections.emptyList()).when(courseMapper).selectList(any(LambdaQueryWrapper.class));

        List<HotCourseResponse> result = hotSpotService.getHotCourses(10);

        assertThat(result).isEmpty();
        verify(learningRecordMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("enrollmentCount 为 null 时按 0 参与计算")
    void hotCoursesNullEnrollmentTreatedAsZero() {
        Course c1 = Course.builder().id(1L).title("课程A").enrollmentCount(null).build();
        Course c2 = Course.builder().id(2L).title("课程B").enrollmentCount(5).build();
        doReturn(List.of(c1, c2)).when(courseMapper).selectList(any(LambdaQueryWrapper.class));

        List<HotCourseResponse> result = hotSpotService.getHotCourses(10);

        assertThat(result.get(0).getCourseId()).isEqualTo(2L);
        assertThat(result.get(1).getHotScore()).isZero();
    }

    @Test
    @DisplayName("topN 参数截断返回条数")
    void hotCoursesRespectsTopN() {
        Course c1 = Course.builder().id(1L).title("课程A").enrollmentCount(100).build();
        Course c2 = Course.builder().id(2L).title("课程B").enrollmentCount(200).build();
        Course c3 = Course.builder().id(3L).title("课程C").enrollmentCount(300).build();
        doReturn(List.of(c1, c2, c3)).when(courseMapper).selectList(any(LambdaQueryWrapper.class));

        List<HotCourseResponse> result = hotSpotService.getHotCourses(2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCourseId()).isEqualTo(3L);
        assertThat(result.get(1).getCourseId()).isEqualTo(2L);
    }

    // ==================== getHotCategories ====================

    @Test
    @DisplayName("按关联已发布课程 enrollmentCount 汇总排序")
    void hotCategoriesAggregatesEnrollmentByCategory() {
        Course c1 = Course.builder().id(1L).enrollmentCount(100).build();
        Course c2 = Course.builder().id(2L).enrollmentCount(50).build();
        Course c3 = Course.builder().id(3L).enrollmentCount(200).build();
        doReturn(List.of(c1, c2, c3)).when(courseMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc1 =
                CategoryCourse.builder().categoryId(1L).courseId(1L).build();
        CategoryCourse cc2 =
                CategoryCourse.builder().categoryId(1L).courseId(2L).build();
        CategoryCourse cc3 =
                CategoryCourse.builder().categoryId(2L).courseId(3L).build();
        doReturn(List.of(cc1, cc2, cc3)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("党建理论");
        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("党史学习");
        doReturn(List.of(cat1, cat2)).when(categoryMapper).selectList(any(LambdaQueryWrapper.class));

        List<HotCategoryResponse> result = hotSpotService.getHotCategories(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryId()).isEqualTo(2L);
        assertThat(result.get(0).getEnrollmentSum()).isEqualTo(200);
        assertThat(result.get(1).getCategoryId()).isEqualTo(1L);
        assertThat(result.get(1).getEnrollmentSum()).isEqualTo(150);
        assertThat(result.get(1).getCourseCount()).isEqualTo(2);
        assertThat(result.get(1).getCategoryName()).isEqualTo("党建理论");
    }

    @Test
    @DisplayName("未发布课程不计入分类汇总")
    void hotCategoriesExcludesUnpublishedCourses() {
        // 只有课程1是已发布（isPublished=true），课程2未发布
        Course c1 = Course.builder().id(1L).enrollmentCount(100).build();
        doReturn(List.of(c1)).when(courseMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc1 =
                CategoryCourse.builder().categoryId(1L).courseId(1L).build();
        CategoryCourse cc2 =
                CategoryCourse.builder().categoryId(1L).courseId(2L).build();
        doReturn(List.of(cc1, cc2)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        List<HotCategoryResponse> result = hotSpotService.getHotCategories(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEnrollmentSum()).isEqualTo(100);
        assertThat(result.get(0).getCourseCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("课程或关联为空时返回空列表")
    void hotCategoriesEmptyDataReturnsEmptyList() {
        doReturn(Collections.emptyList()).when(courseMapper).selectList(any(LambdaQueryWrapper.class));
        assertThat(hotSpotService.getHotCategories(10)).isEmpty();

        Course c1 = Course.builder().id(1L).enrollmentCount(100).build();
        doReturn(List.of(c1)).when(courseMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));
        assertThat(hotSpotService.getHotCategories(10)).isEmpty();
    }

    // ==================== getLearningTrend ====================

    @Test
    @DisplayName("按天分组统计学习人次与总时长并升序返回")
    void learningTrendGroupsByDay() {
        UserLearningRecord d1r1 = UserLearningRecord.builder()
                .startTime(LocalDateTime.of(2026, 8, 1, 10, 0))
                .duration(600)
                .build();
        UserLearningRecord d1r2 = UserLearningRecord.builder()
                .startTime(LocalDateTime.of(2026, 8, 1, 14, 0))
                .duration(300)
                .build();
        UserLearningRecord d2r1 = UserLearningRecord.builder()
                .startTime(LocalDateTime.of(2026, 8, 2, 9, 0))
                .duration(1200)
                .build();
        doReturn(List.of(d1r1, d1r2, d2r1)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        List<LearningTrendResponse> result = hotSpotService.getLearningTrend(30);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate()).isEqualTo(java.time.LocalDate.of(2026, 8, 1));
        assertThat(result.get(0).getLearningCount()).isEqualTo(2);
        assertThat(result.get(0).getTotalDuration()).isEqualTo(900);
        assertThat(result.get(1).getDate()).isEqualTo(java.time.LocalDate.of(2026, 8, 2));
        assertThat(result.get(1).getLearningCount()).isEqualTo(1);
        assertThat(result.get(1).getTotalDuration()).isEqualTo(1200);
    }

    @Test
    @DisplayName("duration 为 null 时按时长0计入")
    void learningTrendNullDurationTreatedAsZero() {
        UserLearningRecord r1 = UserLearningRecord.builder()
                .startTime(LocalDateTime.of(2026, 8, 1, 10, 0))
                .duration(null)
                .build();
        doReturn(List.of(r1)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        List<LearningTrendResponse> result = hotSpotService.getLearningTrend(30);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLearningCount()).isEqualTo(1);
        assertThat(result.get(0).getTotalDuration()).isZero();
    }

    @Test
    @DisplayName("无学习记录时返回空列表")
    void learningTrendEmptyReturnsEmptyList() {
        List<LearningTrendResponse> result = hotSpotService.getLearningTrend(30);

        assertThat(result).isEmpty();
    }
}
