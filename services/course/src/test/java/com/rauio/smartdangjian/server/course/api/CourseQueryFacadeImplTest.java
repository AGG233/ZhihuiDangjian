package com.rauio.smartdangjian.server.course.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.course.api.dto.CourseSummary;
import com.rauio.smartdangjian.server.course.pojo.entity.Course;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.course.service.course.CourseService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseQueryFacadeImpl")
class CourseQueryFacadeImplTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseQueryFacadeImpl facade;

    private Course publishedCourse() {
        return Course.builder()
                .id(1L)
                .title("党建课程")
                .description("课程描述")
                .isPublished(true)
                .build();
    }

    private Course unpublishedCourse() {
        return Course.builder().id(2L).title("未发布课程").isPublished(false).build();
    }

    private CourseResponse publishedResponse() {
        return CourseResponse.builder()
                .id(1L)
                .title("党建课程")
                .description("课程描述")
                .difficulty("beginner")
                .build();
    }

    @Nested
    @DisplayName("get 方法")
    class Get {

        @Test
        @DisplayName("已发布课程返回 CourseResponse")
        void publishedCourseReturnsResponse() {
            when(courseService.getById(1L)).thenReturn(publishedCourse());
            when(courseService.get(1L)).thenReturn(publishedResponse());

            CourseResponse result = facade.get(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("党建课程");
        }

        @Test
        @DisplayName("课程不存在返回 null")
        void notFoundReturnsNull() {
            when(courseService.getById(99L)).thenReturn(null);

            CourseResponse result = facade.get(99L);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("未发布课程返回 null")
        void unpublishedCourseReturnsNull() {
            when(courseService.getById(2L)).thenReturn(unpublishedCourse());

            CourseResponse result = facade.get(2L);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getSummary 方法")
    class GetSummary {

        @Test
        @DisplayName("已发布课程返回 CourseSummary")
        void publishedCourseReturnsSummary() {
            when(courseService.getById(1L)).thenReturn(publishedCourse());

            CourseSummary result = facade.getSummary(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("党建课程");
            assertThat(result.getDescription()).isEqualTo("课程描述");
        }

        @Test
        @DisplayName("未发布课程也返回 CourseSummary（不过滤 isPublished）")
        void unpublishedCourseReturnsSummary() {
            when(courseService.getById(2L)).thenReturn(unpublishedCourse());

            CourseSummary result = facade.getSummary(2L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getTitle()).isEqualTo("未发布课程");
        }

        @Test
        @DisplayName("课程不存在返回 null")
        void notFoundReturnsNull() {
            when(courseService.getById(99L)).thenReturn(null);

            CourseSummary result = facade.getSummary(99L);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("委托方法")
    class DelegationMethods {

        @Test
        @DisplayName("listTopCategoryIdsByCourseIds 委托 courseService")
        void listTopCategoryIdsDelegates() {
            when(courseService.listTopCategoryIdsByCourseIds(List.of(1L, 2L), 5))
                    .thenReturn(List.of(10L, 20L));

            List<Long> result = facade.listTopCategoryIdsByCourseIds(List.of(1L, 2L), 5);

            assertThat(result).containsExactly(10L, 20L);
        }

        @Test
        @DisplayName("recommendPublishedCourseIds 委托 courseService")
        void recommendPublishedCourseIdsDelegates() {
            Page<Long> expected = new Page<>(1, 10, 2);
            expected.setRecords(List.of(1L, 2L));
            when(courseService.recommendPublishedCourseIds(List.of(1L), List.of(3L), "beginner", 1, 10))
                    .thenReturn(expected);

            Page<Long> result = facade.recommendPublishedCourseIds(List.of(1L), List.of(3L), "beginner", 1, 10);

            assertThat(result.getRecords()).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("searchPublishedCourses 委托 courseService")
        void searchPublishedCoursesDelegates() {
            Page<CourseResponse> expected = new Page<>(1, 10, 1);
            expected.setRecords(List.of(publishedResponse()));
            when(courseService.searchPublishedCourses("党建", "1", "beginner", 1, 10))
                    .thenReturn(expected);

            Page<CourseResponse> result = facade.searchPublishedCourses("党建", "1", "beginner", 1, 10);

            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("listCourseResponsesByIds 委托 courseService")
        void listCourseResponsesByIdsDelegates() {
            when(courseService.listCourseResponsesByIds(List.of(1L))).thenReturn(List.of(publishedResponse()));

            List<CourseResponse> result = facade.listCourseResponsesByIds(List.of(1L));

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("getCourseResponseMapByIds 委托 courseService")
        void getCourseResponseMapByIdsDelegates() {
            when(courseService.getCourseResponseMapByIds(List.of(1L))).thenReturn(Map.of(1L, publishedResponse()));

            Map<Long, CourseResponse> result = facade.getCourseResponseMapByIds(List.of(1L));

            assertThat(result).containsKey(1L);
        }
    }

    @Nested
    @DisplayName("searchByTitle 方法")
    class SearchByTitle {

        @Test
        @DisplayName("根据标题关键词返回已发布课程摘要列表")
        void returnsSummariesForMatchingTitle() {
            Course course = Course.builder()
                    .id(1L)
                    .title("党建课程")
                    .description("党建基础知识")
                    .difficulty("beginner")
                    .coverImageId(100L)
                    .enrollmentCount(50)
                    .isPublished(true)
                    .build();
            when(courseService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(course));

            List<CourseSummary> results = facade.searchByTitle("党建", 10);

            assertThat(results).hasSize(1);
            CourseSummary summary = results.get(0);
            assertThat(summary.getId()).isEqualTo(1L);
            assertThat(summary.getTitle()).isEqualTo("党建课程");
            assertThat(summary.getDescription()).isEqualTo("党建基础知识");
            assertThat(summary.getDifficulty()).isEqualTo("beginner");
            assertThat(summary.getCoverImageId()).isEqualTo(100L);
            assertThat(summary.getEnrollmentCount()).isEqualTo(50);
        }

        @Test
        @DisplayName("无匹配时返回空列表")
        void noMatchReturnsEmpty() {
            when(courseService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

            List<CourseSummary> results = facade.searchByTitle("不存在", 10);

            assertThat(results).isEmpty();
        }
    }
}
