package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;

import java.util.Collections;
import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchService 全文搜索与混合搜索")
class SearchServiceTest {

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, Course.class);
        TableInfoHelper.initTableInfo(assistant, CategoryCourse.class);
    }

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private UserService userService;

    @Mock
    private CourseConvertor courseConvertor;

    @Mock
    private CategoryCourseMapper categoryCourseMapper;

    @Mock
    private RecommendService recommendService;

    @Spy
    @InjectMocks
    private SearchService searchService;

    @BeforeEach
    void resetSpy() {
        reset(searchService);
    }

    // ==================== searchCourses ====================

    @Test
    @DisplayName("仅传关键词时执行全文检索并返回课程列表")
    void searchCoursesWithKeywordPerformsFulltextSearch() {
        Course course = Course.builder().id(1L).title("测试课程").description("描述").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        CourseResponse response = CourseResponse.builder().id(1L).title("测试课程").build();
        doReturn(List.of(response)).when(courseConvertor).toResponseList(any());

        doReturn(Collections.emptyList()).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Page<CourseResponse> result = searchService.searchCourses("测试", null, null, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTitle()).isEqualTo("测试课程");
    }

    @Test
    @DisplayName("传分类ID时先查分类关联再过滤课程")
    void searchCoursesWithCategoryIdFiltersByCategory() {
        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id(1L).title("分类课程").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        CourseResponse response = CourseResponse.builder().id(1L).title("分类课程").build();
        doReturn(List.of(response)).when(courseConvertor).toResponseList(any());

        Page<CourseResponse> result = searchService.searchCourses(null, "cat-1", null, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("分类下无关联课程时直接返回空页")
    void searchCoursesWithCategoryNoMatchesReturnsEmptyPage() {
        doReturn(Collections.emptyList()).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Page<CourseResponse> result = searchService.searchCourses(null, "cat-empty", null, 1, 10);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isZero();
    }

    @Test
    @DisplayName("传难度时过滤指定难度的课程")
    void searchCoursesWithDifficultyFiltersByDifficulty() {
        Course course = Course.builder().id(1L).difficulty("hard").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        CourseResponse response =
                CourseResponse.builder().id(1L).difficulty("hard").build();
        doReturn(List.of(response)).when(courseConvertor).toResponseList(any());
        doReturn(Collections.emptyList()).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Page<CourseResponse> result = searchService.searchCourses(null, null, "hard", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getDifficulty()).isEqualTo("hard");
    }

    @Test
    @DisplayName("无任何过滤条件时返回所有已发布课程")
    void searchCoursesNoFiltersReturnsAllPublished() {
        Page<Course> coursePage = new Page<>(1, 10, 0);
        coursePage.setRecords(Collections.emptyList());
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<CourseResponse> result = searchService.searchCourses(null, null, null, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("空白关键词不触发全文检索但仍查询已发布课程")
    void searchCoursesWithBlankKeywordQueriesPublishedCourses() {
        Page<Course> coursePage = new Page<>(1, 10, 0);
        coursePage.setRecords(Collections.emptyList());
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<CourseResponse> result = searchService.searchCourses("   ", null, null, 1, 10);

        assertThat(result.getRecords()).isEmpty();
        verify(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("非法分页参数由 MyBatis-Plus Page 规范化")
    void searchCoursesNormalizesInvalidPaginationValues() {
        Page<Course> coursePage = new Page<>(0, -1, 0);
        coursePage.setRecords(Collections.emptyList());
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        searchService.searchCourses(null, null, null, 0, -1);

        ArgumentCaptor<Page<Course>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(courseMapper).selectPage(pageCaptor.capture(), any(LambdaQueryWrapper.class));
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(-1);
    }

    @Test
    @DisplayName("搜索结果包含分类ID映射")
    void searchCoursesEnrichesWithCategoryIds() {
        Course course = Course.builder().id(1L).title("课程1").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        CourseResponse response = CourseResponse.builder().id(1L).title("课程1").build();
        doReturn(List.of(response)).when(courseConvertor).toResponseList(any());

        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Page<CourseResponse> result = searchService.searchCourses("测试", null, null, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    // ==================== hybridSearch ====================

    @Test
    @DisplayName("搜索结果数量充足时不补充推荐")
    void hybridSearchWithEnoughResultsDoesNotSupplement() {
        Course course = Course.builder().id(1L).title("搜索结果").build();
        Page<Course> coursePage = new Page<>(1, 1, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        CourseResponse r1 = CourseResponse.builder().id(1L).title("搜索结果").build();
        doReturn(List.of(r1)).when(courseConvertor).toResponseList(any());
        doReturn(Collections.emptyList()).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Page<CourseResponse> result = searchService.hybridSearch("关键词", 1, 1);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1L);
        verify(recommendService, never()).recommend(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("搜索结果不足时用个性化推荐补充")
    void hybridSearchWithInsufficientResultsSupplementsWithRecommendations() {
        Course course1 = Course.builder().id(1L).title("搜索1").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course1));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        CourseResponse r1 = CourseResponse.builder().id(1L).title("搜索1").build();
        CourseResponse r2 = CourseResponse.builder().id(2L).title("推荐课程").build();
        doReturn(List.of(r1), List.of(r2)).when(courseConvertor).toResponseList(any());

        CategoryCourse cc1 =
                CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        CategoryCourse cc2 =
                CategoryCourse.builder().courseId(2L).categoryId(1L).build();
        doReturn(List.of(cc1), List.of(cc2)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        doReturn("1").when(userService).getCurrentUserId();

        Page<Long> recPage = new Page<>(1, 10, 1);
        recPage.setRecords(List.of(2L));
        doReturn(recPage).when(recommendService).recommend(1L, 1, 10);

        Course recommendedCourse = Course.builder().id(2L).title("推荐课程").build();
        doReturn(List.of(recommendedCourse)).when(courseMapper).selectBatchIds(any());

        Page<CourseResponse> result = searchService.hybridSearch("关键词", 1, 10);

        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1L);
        assertThat(result.getRecords().get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("搜索结果不足但推荐无新课程时不重复添加")
    void hybridSearchWithInsufficientResultsButNoNewRecommendations() {
        Course course1 = Course.builder().id(1L).title("搜索1").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course1));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        CourseResponse r1 = CourseResponse.builder().id(1L).title("搜索1").build();
        doReturn(List.of(r1)).when(courseConvertor).toResponseList(any());

        CategoryCourse cc1 =
                CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        doReturn(List.of(cc1)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        doReturn("1").when(userService).getCurrentUserId();

        Page<Long> recPage = new Page<>(1, 10, 1);
        recPage.setRecords(List.of(1L));
        doReturn(recPage).when(recommendService).recommend(1L, 1, 10);

        Page<CourseResponse> result = searchService.hybridSearch("关键词", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("混合搜索当前用户 ID 为 null 时透传解析异常")
    void hybridSearchWithNullCurrentUserIdPropagatesParseFailure() {
        Page<Course> coursePage = new Page<>(1, 10, 0);
        coursePage.setRecords(Collections.emptyList());
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        doReturn(null).when(userService).getCurrentUserId();

        assertThatThrownBy(() -> searchService.hybridSearch("关键词", 1, 10)).isInstanceOf(RuntimeException.class);
        verify(recommendService, never()).recommend(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("混合搜索当前用户 ID 非数字时透传解析异常")
    void hybridSearchWithInvalidCurrentUserIdPropagatesParseFailure() {
        Page<Course> coursePage = new Page<>(1, 10, 0);
        coursePage.setRecords(Collections.emptyList());
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        doReturn("not-a-number").when(userService).getCurrentUserId();

        assertThatThrownBy(() -> searchService.hybridSearch("关键词", 1, 10)).isInstanceOf(RuntimeException.class);
        verify(recommendService, never()).recommend(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("推荐服务异常时混合搜索透传异常")
    void hybridSearchPropagatesRecommendServiceException() {
        Page<Course> coursePage = new Page<>(1, 10, 0);
        coursePage.setRecords(Collections.emptyList());
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        doReturn("1").when(userService).getCurrentUserId();
        doThrow(new IllegalStateException("recommend failed"))
                .when(recommendService)
                .recommend(1L, 1, 10);

        assertThatThrownBy(() -> searchService.hybridSearch("关键词", 1, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("recommend failed");
    }

    @Test
    @DisplayName("课程 ID 全为 null 时 getCategoryIdMap 处理空集合")
    void searchCoursesWithNullCourseIdsHandlesEmptySet() {
        Course course = Course.builder().id(null).title("无ID课程").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        CourseResponse response = CourseResponse.builder().title("无ID课程").build();
        doReturn(List.of(response)).when(courseConvertor).toResponseList(any());

        Page<CourseResponse> result = searchService.searchCourses("测试", null, null, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getCategoryId()).isNull();
    }

    @Test
    @DisplayName("返回null记录时toCourseResponseList处理为空列表")
    void searchCoursesWithNullRecordsReturnsEmpty() {
        Page<Course> coursePage = new Page<>(1, 10, 0);
        coursePage.setRecords(null);
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<CourseResponse> result = searchService.searchCourses("测试", null, null, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }
}
