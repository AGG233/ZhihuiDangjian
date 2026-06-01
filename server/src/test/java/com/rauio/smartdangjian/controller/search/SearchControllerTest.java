package com.rauio.smartdangjian.controller.search;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.search.controller.SearchController;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.search.service.RecommendService;
import com.rauio.smartdangjian.server.search.service.SearchService;
import com.rauio.smartdangjian.server.search.service.UserProfileService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SearchControllerTest.TestConfig.class)
@DisplayName("搜索与推荐接口测试")
class SearchControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public SearchController searchController(
                SearchService searchService, RecommendService recommendService, UserProfileService userProfileService) {
            return new SearchController(searchService, recommendService, userProfileService);
        }
    }

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private RecommendService recommendService;

    @MockitoBean
    private UserProfileService userProfileService;

    private Page<CourseResponse> createCoursePage(int count) {
        Page<CourseResponse> page = new Page<>(1, 10, count);
        if (count > 0) {
            page.setRecords(List.of(CourseTestDataFactory.createCourseResponse(1L)));
        } else {
            page.setRecords(List.of());
        }
        return page;
    }

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /courses - 搜索课程成功")
        void searchCoursesSuccess() throws Exception {
            when(searchService.searchCourses(anyString(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(createCoursePage(1));

            mockMvc.perform(get("/api/search/courses")
                            .param("keyword", "党建")
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.records.length()").value(1));
        }

        @Test
        @DisplayName("GET /hybrid - 混合搜索成功")
        void hybridSearchSuccess() throws Exception {
            when(searchService.hybridSearch(anyString(), anyInt(), anyInt())).thenReturn(createCoursePage(1));

            mockMvc.perform(get("/api/search/hybrid")
                            .param("keyword", "党建")
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.records.length()").value(1));
        }

        @Test
        @DisplayName("GET /recommend - 个性化推荐成功")
        void recommendSuccess() throws Exception {
            UserProfileResponse profile =
                    UserProfileResponse.builder().userId("1").build();
            when(userProfileService.getCurrentUserProfile()).thenReturn(profile);

            Page<Long> recommendPage = new Page<>(1, 10, 2);
            recommendPage.setRecords(List.of(1L, 2L));
            when(recommendService.recommend(eq(1L), anyInt(), anyInt())).thenReturn(recommendPage);

            mockMvc.perform(get("/api/search/recommend").param("pageNum", "1").param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.records.length()").value(2));
        }

        @Test
        @DisplayName("GET /profile - 获取用户画像成功")
        void getProfileSuccess() throws Exception {
            UserProfileResponse profile = UserProfileResponse.builder()
                    .userId("1")
                    .learning(UserProfileResponse.LearningStats.builder()
                            .totalDuration(3600)
                            .avgDuration(600)
                            .totalRecords(6)
                            .completedChapters(4)
                            .build())
                    .build();
            when(userProfileService.getCurrentUserProfile()).thenReturn(profile);

            mockMvc.perform(get("/api/search/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.userId").value("1"));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400")
        void serviceThrowsBusinessException() throws Exception {
            when(searchService.searchCourses(anyString(), any(), any(), anyInt(), anyInt()))
                    .thenThrow(new BusinessException(4000, "搜索服务异常"));

            mockMvc.perform(get("/api/search/courses").param("keyword", "test"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("搜索服务异常"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(userProfileService.getCurrentUserProfile()).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/search/profile"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /courses - 无关键词搜索（空结果）")
        void searchCoursesWithoutKeyword() throws Exception {
            when(searchService.searchCourses(any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(createCoursePage(0));

            mockMvc.perform(get("/api/search/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.records.length()").value(0));
        }

        @Test
        @DisplayName("GET /courses - 带分类和难度过滤")
        void searchCoursesWithFilters() throws Exception {
            when(searchService.searchCourses(eq("党建"), eq("cat-1"), eq("easy"), eq(1), eq(10)))
                    .thenReturn(createCoursePage(1));

            mockMvc.perform(get("/api/search/courses")
                            .param("keyword", "党建")
                            .param("categoryId", "cat-1")
                            .param("difficulty", "easy")
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("GET /recommend - 推荐结果为空")
        void recommendEmpty() throws Exception {
            UserProfileResponse profile =
                    UserProfileResponse.builder().userId("1").build();
            when(userProfileService.getCurrentUserProfile()).thenReturn(profile);

            Page<Long> emptyPage = new Page<>(1, 10, 0);
            emptyPage.setRecords(List.of());
            when(recommendService.recommend(anyLong(), anyInt(), anyInt())).thenReturn(emptyPage);

            mockMvc.perform(get("/api/search/recommend"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.records.length()").value(0));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @ParameterizedTest(name = "keyword={0}")
        @ValueSource(strings = {"<script>alert('xss')</script>", "' OR '1'='1"})
        @DisplayName("特殊 keyword 参数作为字面量透传给搜索服务")
        void specialKeywordIsPassedAsLiteral(String keyword) throws Exception {
            when(searchService.searchCourses(anyString(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(createCoursePage(0));

            mockMvc.perform(get("/api/search/courses").param("keyword", keyword))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            verify(searchService).searchCourses(eq(keyword), any(), any(), eq(1), eq(10));
        }

        @ParameterizedTest(name = "POST {0}")
        @CsvSource({"/api/search/courses", "/api/search/recommend"})
        @DisplayName("POST 请求 GET-only 接口返回 405")
        void postToGetOnlyEndpointReturns405(String path) throws Exception {
            mockMvc.perform(post(path)).andExpect(status().isMethodNotAllowed());
        }
    }
}
