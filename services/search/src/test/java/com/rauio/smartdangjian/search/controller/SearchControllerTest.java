package com.rauio.smartdangjian.search.controller;

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
import com.rauio.smartdangjian.search.pojo.vo.UserProfileVO;
import com.rauio.smartdangjian.search.service.RecommendService;
import com.rauio.smartdangjian.search.service.SearchService;
import com.rauio.smartdangjian.search.service.UserProfileService;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @Mock
    private RecommendService recommendService;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private SearchController searchController;

    @Test
    @DisplayName("searchCourses 委托 Service 搜索课程")
    void searchCoursesDelegates() {
        Page<CourseVO> page = new Page<>(1, 10);
        page.setRecords(List.of(CourseVO.builder().id("c-1").title("测试课程").build()));
        when(searchService.searchCourses("测试", "cat-1", "medium", 1, 10)).thenReturn(page);

        var result = searchController.searchCourses("测试", "cat-1", "medium", 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getData().getRecords()).hasSize(1);
        assertThat(result.getData().getRecords().get(0).getTitle()).isEqualTo("测试课程");
    }

    @Test
    @DisplayName("searchCourses 只传 keyword 时仍正常搜索")
    void searchCoursesWithOnlyKeyword() {
        Page<CourseVO> page = new Page<>(1, 10);
        page.setRecords(List.of());
        when(searchService.searchCourses("测试", null, null, 1, 10)).thenReturn(page);

        var result = searchController.searchCourses("测试", null, null, 1, 10);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("hybridSearch 委托 Service 混合搜索")
    void hybridSearchDelegates() {
        Page<CourseVO> page = new Page<>(1, 10);
        when(searchService.hybridSearch("关键词", 1, 10)).thenReturn(page);

        var result = searchController.hybridSearch("关键词", 1, 10);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("recommend 委托 Service 返回推荐")
    void recommendDelegates() {
        UserProfileVO profile = UserProfileVO.builder().userId("user-1").build();
        when(userProfileService.getCurrentUserProfile()).thenReturn(profile);
        Page<String> recPage = new Page<>(1, 10);
        recPage.setRecords(List.of("course-1"));
        when(recommendService.recommend("user-1", 1, 10)).thenReturn(recPage);

        var result = searchController.recommend(1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getData().getRecords()).contains("course-1");
    }

    @Test
    @DisplayName("getProfile 委托 Service 返回用户画像")
    void getProfileDelegates() {
        UserProfileVO profile = UserProfileVO.builder().userId("user-1").build();
        when(userProfileService.getCurrentUserProfile()).thenReturn(profile);

        var result = searchController.getProfile();

        assertThat(result).isNotNull();
        assertThat(result.getData().getUserId()).isEqualTo("user-1");
    }
}
