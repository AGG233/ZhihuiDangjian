package com.rauio.smartdangjian.server.search.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.search.pojo.response.HotCategoryResponse;
import com.rauio.smartdangjian.server.search.pojo.response.HotCourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;
import com.rauio.smartdangjian.server.search.service.LearningHotspotService;

@ExtendWith(MockitoExtension.class)
@DisplayName("学习热点接口测试")
class LearningHotspotControllerTest {

    @Mock
    private LearningHotspotService learningHotspotService;

    @InjectMocks
    private LearningHotspotController controller;

    @Nested
    @DisplayName("获取热门课程")
    class HotCoursesTests {

        @Test
        @DisplayName("获取热门课程列表成功")
        void getHotCoursesSuccess() {
            when(learningHotspotService.getHotCourses(anyInt()))
                    .thenReturn(List.of(HotCourseResponse.builder()
                            .courseId(1L)
                            .title("课程1")
                            .build()));

            var result = controller.getHotCourses(10);

            assertThat(result.getData()).hasSize(1);
            assertThat(result.getData().get(0).getTitle()).isEqualTo("课程1");
        }

        @Test
        @DisplayName("热门课程为空时返回空列表")
        void getHotCoursesEmpty() {
            when(learningHotspotService.getHotCourses(anyInt())).thenReturn(List.of());

            var result = controller.getHotCourses(10);

            assertThat(result.getData()).isEmpty();
        }
    }

    @Nested
    @DisplayName("获取热门分类")
    class HotCategoriesTests {

        @Test
        @DisplayName("获取热门分类列表成功")
        void getHotCategoriesSuccess() {
            when(learningHotspotService.getHotCategories(anyInt()))
                    .thenReturn(List.of(HotCategoryResponse.builder()
                            .categoryId(1L)
                            .name("党建")
                            .build()));

            var result = controller.getHotCategories(5);

            assertThat(result.getData()).hasSize(1);
            assertThat(result.getData().get(0).getName()).isEqualTo("党建");
        }

        @Test
        @DisplayName("热门分类为空时返回空列表")
        void getHotCategoriesEmpty() {
            when(learningHotspotService.getHotCategories(anyInt())).thenReturn(List.of());

            var result = controller.getHotCategories(5);

            assertThat(result.getData()).isEmpty();
        }
    }

    @Nested
    @DisplayName("获取学习趋势")
    class TrendsTests {

        @Test
        @DisplayName("获取学习趋势成功")
        void getTrendsSuccess() {
            var trend =
                    LearningTrendResponse.builder().days(7).dailyData(List.of()).build();
            when(learningHotspotService.getTrends(anyInt())).thenReturn(trend);

            var result = controller.getTrends(7);

            assertThat(result.getData().getDays()).isEqualTo(7);
        }

        @Test
        @DisplayName("指定天数为 1 时返回单日数据")
        void getTrendsSingleDay() {
            var trend =
                    LearningTrendResponse.builder().days(1).dailyData(List.of()).build();
            when(learningHotspotService.getTrends(anyInt())).thenReturn(trend);

            var result = controller.getTrends(1);

            assertThat(result.getData().getDays()).isEqualTo(1);
        }
    }
}
