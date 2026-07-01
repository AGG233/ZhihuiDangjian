package com.rauio.smartdangjian.crosslayer.search;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.search.controller.LearningHotspotController;
import com.rauio.smartdangjian.server.search.controller.SearchController;
import com.rauio.smartdangjian.server.search.pojo.response.HotCategoryResponse;
import com.rauio.smartdangjian.server.search.pojo.response.HotCourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.search.service.LearningHotspotService;
import com.rauio.smartdangjian.server.search.service.RecommendService;
import com.rauio.smartdangjian.server.search.service.SearchService;
import com.rauio.smartdangjian.server.search.service.UserProfileService;

@SpringBootTest(classes = SearchControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("搜索推荐和学习热点控制层集成测试")
class SearchControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private RecommendService recommendService;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private LearningHotspotService learningHotspotService;

    @BeforeEach
    void setUp() {
        reset(searchService, recommendService, userProfileService, learningHotspotService);
        setStudentContext(1L, "uni-1");
    }

    // ==================== SearchController ====================

    @Test
    @DisplayName("GET /api/search/courses 成功搜索课程")
    void searchCourses() throws Exception {
        Page<CourseResponse> page = new Page<>(1, 10);
        page.setRecords(List.of(CourseResponse.builder().id(1L).title("党史课程").build()));
        page.setTotal(1);
        when(searchService.searchCourses("党史", null, null, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/search/courses")
                        .param("keyword", "党史")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("党史课程"));

        verify(searchService).searchCourses("党史", null, null, 1, 10);
    }

    @Test
    @DisplayName("GET /api/search/courses 使用所有筛选参数")
    void searchCoursesWithAllFilters() throws Exception {
        Page<CourseResponse> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(searchService.searchCourses("keyword", "5", "beginner", 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/search/courses")
                        .param("keyword", "keyword")
                        .param("categoryId", "5")
                        .param("difficulty", "beginner")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @DisplayName("GET /api/search/hybrid 成功执行混合搜索")
    void hybridSearch() throws Exception {
        Page<CourseResponse> page = new Page<>(1, 10);
        page.setRecords(List.of(CourseResponse.builder().id(1L).title("混合结果").build()));
        page.setTotal(1);
        when(searchService.hybridSearch("党史", 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/search/hybrid")
                        .param("keyword", "党史")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(searchService).hybridSearch("党史", 1, 10);
    }

    @Test
    @DisplayName("GET /api/search/recommend 成功获取个性化推荐")
    void recommend() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder().userId("1").build();
        Page<CourseResponse> page = new Page<>(1, 10);
        page.setRecords(List.of(
                CourseResponse.builder().id(100L).title("推荐课程A").build(),
                CourseResponse.builder().id(101L).title("推荐课程B").build()));
        page.setTotal(2);
        when(userProfileService.getCurrentUserProfile()).thenReturn(profile);
        when(recommendService.recommend(1L, 1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/search/recommend").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].title").value("推荐课程A"));
    }

    @Test
    @DisplayName("GET /api/search/profile 成功获取用户画像")
    void getProfile() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .learning(UserProfileResponse.LearningStats.builder()
                        .totalDuration(3600)
                        .avgDuration(600)
                        .totalRecords(12)
                        .completedChapters(8)
                        .build())
                .build();
        when(userProfileService.getCurrentUserProfile()).thenReturn(profile);

        mockMvc.perform(get("/api/search/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.userId").value("1"))
                .andExpect(jsonPath("$.data.learning.totalDuration").value(3600));

        verify(userProfileService).getCurrentUserProfile();
    }

    // ==================== LearningHotspotController ====================

    @Test
    @DisplayName("GET /api/learning/hotspots/courses 成功获取热门课程")
    void getHotCourses() throws Exception {
        HotCourseResponse course = HotCourseResponse.builder()
                .courseId(1L)
                .title("热门课程")
                .learnerCount(100)
                .build();
        when(learningHotspotService.getHotCourses(10)).thenReturn(List.of(course));

        mockMvc.perform(get("/api/learning/hotspots/courses").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].title").value("热门课程"))
                .andExpect(jsonPath("$.data[0].learnerCount").value(100));
    }

    @Test
    @DisplayName("GET /api/learning/hotspots/categories 成功获取热门分类")
    void getHotCategories() throws Exception {
        HotCategoryResponse category = HotCategoryResponse.builder()
                .categoryId(1L)
                .name("热门分类")
                .learnerCount(50)
                .build();
        when(learningHotspotService.getHotCategories(5)).thenReturn(List.of(category));

        mockMvc.perform(get("/api/learning/hotspots/categories").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].name").value("热门分类"));
    }

    @Test
    @DisplayName("GET /api/learning/hotspots/trends 成功获取学习趋势")
    void getTrends() throws Exception {
        LearningTrendResponse trend = LearningTrendResponse.builder()
                .dailyData(List.of(
                        LearningTrendResponse.DailyCount.builder()
                                .date("2026-06-01")
                                .count(10)
                                .build(),
                        LearningTrendResponse.DailyCount.builder()
                                .date("2026-06-02")
                                .count(20)
                                .build()))
                .build();
        when(learningHotspotService.getTrends(7)).thenReturn(trend);

        mockMvc.perform(get("/api/learning/hotspots/trends").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.dailyData[0].count").value(10));
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        SearchController searchController(
                SearchService searchService, RecommendService recommendService, UserProfileService userProfileService) {
            return new SearchController(searchService, recommendService, userProfileService);
        }

        @Bean
        LearningHotspotController learningHotspotController(LearningHotspotService learningHotspotService) {
            return new LearningHotspotController(learningHotspotService);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }
}
