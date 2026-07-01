package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.search.api.SearchQueryFacade;

@ExtendWith(MockitoExtension.class)
class RecommendToolTest {

    @Mock
    private SearchQueryFacade searchQueryFacade;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private RecommendTool recommendTool;

    @Test
    @DisplayName("getRecommendedCourses 返回推荐课程 ID 列表字符串")
    void getRecommendedCourses() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        Page<CourseResponse> page = new Page<>();
        page.setRecords(List.of(
                CourseResponse.builder().id(1L).title("课程1").build(),
                CourseResponse.builder().id(2L).title("课程2").build(),
                CourseResponse.builder().id(3L).title("课程3").build()));
        when(searchQueryFacade.recommend(1L, 1, 5)).thenReturn(page);

        String result = recommendTool.getRecommendedCourses(5);

        assertThat(result).contains("1", "2", "3");
    }

    @Test
    @DisplayName("getRecommendedCourses 默认返回 10 条推荐")
    void getRecommendedCoursesDefaultLimit() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        Page<CourseResponse> page = new Page<>();
        page.setRecords(List.of(CourseResponse.builder().id(1L).title("课程1").build()));
        when(searchQueryFacade.recommend(1L, 1, 10)).thenReturn(page);

        String result = recommendTool.getRecommendedCourses(null);

        assertThat(result).contains("1");
    }

    @Test
    @DisplayName("getRecommendedCourses with limit=0 falls back to default 10")
    void getRecommendedCoursesZeroLimit() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        Page<CourseResponse> page = new Page<>();
        page.setRecords(List.of(CourseResponse.builder().id(1L).title("课程1").build()));
        when(searchQueryFacade.recommend(1L, 1, 10)).thenReturn(page);

        String result = recommendTool.getRecommendedCourses(0);

        assertThat(result).contains("1");
    }

    @Test
    @DisplayName("getRecommendedCourses no recommendation returns hint message")
    void getRecommendedCoursesEmpty() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        Page<CourseResponse> page = new Page<>();
        page.setRecords(List.of());
        when(searchQueryFacade.recommend(1L, 1, 10)).thenReturn(page);

        String result = recommendTool.getRecommendedCourses(null);

        assertThat(result).contains("暂无推荐课程");
    }

    @Test
    @DisplayName("getRecommendedCourses 当前用户缺失时以 null userId 推荐")
    void getRecommendedCoursesWithMissingCurrentUser() {
        Page<CourseResponse> page = new Page<>();
        page.setRecords(List.of(CourseResponse.builder().id(9L).title("课程9").build()));
        when(searchQueryFacade.recommend(null, 1, 3)).thenReturn(page);

        String result = recommendTool.getRecommendedCourses(3);

        assertThat(result).contains("9");
    }

    @Test
    @DisplayName("getRecommendedCourses 负数 limit 回退到默认 10")
    void getRecommendedCoursesNegativeLimit() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        Page<CourseResponse> page = new Page<>();
        page.setRecords(List.of(CourseResponse.builder().id(1L).title("课程1").build()));
        when(searchQueryFacade.recommend(1L, 1, 10)).thenReturn(page);

        String result = recommendTool.getRecommendedCourses(-1);

        assertThat(result).contains("1");
    }
}
