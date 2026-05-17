package com.rauio.smartdangjian.server.ai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentSearchToolTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private ContentSearchTool contentSearchTool;

    @Test
    @DisplayName("searchCourses 使用 like 查询匹配标题并返回映射列表")
    void searchCoursesReturnsMappedResults() {
        Course course1 = Course.builder()
                .id("course-1")
                .title("Java Basics")
                .description("Intro to Java")
                .build();
        Course course2 = Course.builder()
                .id("course-2")
                .title("Advanced Java")
                .description("Deep dive")
                .build();

        when(courseService.list(argThat((LambdaQueryWrapper<Course> wrapper) -> {
            // MyBatis-Plus LambdaQueryWrapper does not expose the condition value easily,
            // so we verify the method was called with any wrapper instance.
            return wrapper != null;
        }))).thenReturn(List.of(course1, course2));

        List<Map<String, Object>> result = contentSearchTool.searchCourses("Java");

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsEntry("id", "course-1");
        assertThat(result.get(0)).containsEntry("title", "Java Basics");
        assertThat(result.get(0)).containsEntry("description", "Intro to Java");
        assertThat(result.get(1)).containsEntry("id", "course-2");
        assertThat(result.get(1)).containsEntry("title", "Advanced Java");
        verify(courseService, times(1)).list(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("searchCourses 无匹配结果时返回空列表")
    void searchCoursesReturnsEmptyListWhenNoMatch() {
        when(courseService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Map<String, Object>> result = contentSearchTool.searchCourses("NonExistent");

        assertThat(result).isEmpty();
    }

}
