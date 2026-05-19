package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.search.service.RecommendService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class RecommendToolTest {

    @Mock
    private RecommendService recommendService;

    @Mock
    private UserService userService;

    @InjectMocks
    private RecommendTool recommendTool;

    @Test
    @DisplayName("getRecommendedCourses 返回推荐课程 ID 列表字符串")
    void getRecommendedCourses() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");

        Page<String> page = new Page<>();
        page.setRecords(List.of("course-1", "course-2", "course-3"));
        when(recommendService.recommend("user-1", 1, 5)).thenReturn(page);

        String result = recommendTool.getRecommendedCourses(5, toolContext);

        assertThat(result).contains("course-1", "course-2", "course-3");
    }

    @Test
    @DisplayName("getRecommendedCourses 默认返回 10 条推荐")
    void getRecommendedCoursesDefaultLimit() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");

        Page<String> page = new Page<>();
        page.setRecords(List.of("course-1"));
        when(recommendService.recommend("user-1", 1, 10)).thenReturn(page);

        String result = recommendTool.getRecommendedCourses(null, toolContext);

        assertThat(result).contains("course-1");
    }

    @Test
    @DisplayName("getRecommendedCourses 无推荐时返回提示消息")
    void getRecommendedCoursesEmpty() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");

        Page<String> page = new Page<>();
        page.setRecords(List.of());
        when(recommendService.recommend("user-1", 1, 10)).thenReturn(page);

        String result = recommendTool.getRecommendedCourses(null, toolContext);

        assertThat(result).contains("暂无推荐课程");
    }
}
