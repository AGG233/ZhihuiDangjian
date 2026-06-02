package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchService 全文搜索与混合搜索")
class SearchServiceTest {

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @Mock
    private RecommendService recommendService;

    @InjectMocks
    private SearchService searchService;

    @Test
    @DisplayName("searchCourses 委托课程领域查询已发布课程")
    void searchCoursesDelegatesToCourseService() {
        Page<CourseResponse> page = pageOf(List.of(course(1L, "测试课程")), 1, 10, 1);
        doReturn(page).when(courseService).searchPublishedCourses("测试", "1", "hard", 1, 10);

        Page<CourseResponse> result = searchService.searchCourses("测试", "1", "hard", 1, 10);

        assertThat(result).isSameAs(page);
        verify(courseService).searchPublishedCourses("测试", "1", "hard", 1, 10);
    }

    @Test
    @DisplayName("搜索结果数量充足时不补充推荐")
    void hybridSearchWithEnoughResultsDoesNotSupplement() {
        Page<CourseResponse> page = pageOf(List.of(course(1L, "搜索结果")), 1, 1, 1);
        doReturn(page).when(courseService).searchPublishedCourses("关键词", null, null, 1, 1);

        Page<CourseResponse> result = searchService.hybridSearch("关键词", 1, 1);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1L);
        verify(recommendService, never()).recommend(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("搜索结果不足时用个性化推荐补充")
    void hybridSearchWithInsufficientResultsSupplementsWithRecommendations() {
        Page<CourseResponse> page = pageOf(List.of(course(1L, "搜索1")), 1, 10, 1);
        doReturn(page).when(courseService).searchPublishedCourses("关键词", null, null, 1, 10);
        doReturn("1").when(userService).getCurrentUserId();

        Page<Long> recPage = new Page<>(1, 10, 2);
        recPage.setRecords(List.of(1L, 2L, 3L));
        doReturn(recPage).when(recommendService).recommend(1L, 1, 10);
        doReturn(List.of(course(2L, "推荐2"), course(3L, "推荐3")))
                .when(courseService)
                .listCourseResponsesByIds(Set.of(2L, 3L));

        Page<CourseResponse> result = searchService.hybridSearch("关键词", 1, 10);

        assertThat(result.getRecords()).extracting(CourseResponse::getId).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("搜索结果不足但推荐无新课程时不重复添加")
    void hybridSearchWithInsufficientResultsButNoNewRecommendations() {
        Page<CourseResponse> page = pageOf(List.of(course(1L, "搜索1")), 1, 10, 1);
        doReturn(page).when(courseService).searchPublishedCourses("关键词", null, null, 1, 10);
        doReturn("1").when(userService).getCurrentUserId();

        Page<Long> recPage = new Page<>(1, 10, 1);
        recPage.setRecords(List.of(1L));
        doReturn(recPage).when(recommendService).recommend(1L, 1, 10);

        Page<CourseResponse> result = searchService.hybridSearch("关键词", 1, 10);

        assertThat(result.getRecords()).extracting(CourseResponse::getId).containsExactly(1L);
        verify(courseService, never()).listCourseResponsesByIds(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("混合搜索当前用户 ID 为 null 时透传解析异常")
    void hybridSearchWithNullCurrentUserIdPropagatesParseFailure() {
        doReturn(pageOf(List.of(), 1, 10, 0)).when(courseService).searchPublishedCourses("关键词", null, null, 1, 10);
        doReturn(null).when(userService).getCurrentUserId();

        assertThatThrownBy(() -> searchService.hybridSearch("关键词", 1, 10)).isInstanceOf(RuntimeException.class);
        verify(recommendService, never()).recommend(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("混合搜索当前用户 ID 非数字时透传解析异常")
    void hybridSearchWithInvalidCurrentUserIdPropagatesParseFailure() {
        doReturn(pageOf(List.of(), 1, 10, 0)).when(courseService).searchPublishedCourses("关键词", null, null, 1, 10);
        doReturn("not-a-number").when(userService).getCurrentUserId();

        assertThatThrownBy(() -> searchService.hybridSearch("关键词", 1, 10)).isInstanceOf(RuntimeException.class);
        verify(recommendService, never()).recommend(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("推荐服务异常时混合搜索透传异常")
    void hybridSearchPropagatesRecommendServiceException() {
        doReturn(pageOf(List.of(), 1, 10, 0)).when(courseService).searchPublishedCourses("关键词", null, null, 1, 10);
        doReturn("1").when(userService).getCurrentUserId();
        doThrow(new IllegalStateException("recommend failed"))
                .when(recommendService)
                .recommend(1L, 1, 10);

        assertThatThrownBy(() -> searchService.hybridSearch("关键词", 1, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("recommend failed");
    }

    private static Page<CourseResponse> pageOf(List<CourseResponse> records, long current, long size, long total) {
        Page<CourseResponse> page = new Page<>(current, size, total);
        page.setRecords(records);
        return page;
    }

    private static CourseResponse course(Long id, String title) {
        return CourseResponse.builder().id(id).title(title).build();
    }
}
