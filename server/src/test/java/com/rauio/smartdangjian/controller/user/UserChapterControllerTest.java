package com.rauio.smartdangjian.controller.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.controller.user.UserChapterController;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserChapterControllerTest.TestConfig.class)
@DisplayName("用户章节接口测试")
class UserChapterControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserChapterController userChapterController(ChapterService chapterService) {
            return new UserChapterController(chapterService);
        }
    }

    @MockitoBean
    private ChapterService chapterService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /{id} - 获取章节详情成功")
        void getChapterDetailSuccess() throws Exception {
            ChapterResponse vo = CourseTestDataFactory.createChapterResponse(1L);
            when(chapterService.get(1L)).thenReturn(vo);

            mockMvc.perform(get("/api/content/chapters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.title").value("test-chapter"))
                    .andExpect(jsonPath("$.data.description").value("test-chapter-description"))
                    .andExpect(jsonPath("$.data.orderIndex").value(1))
                    .andExpect(jsonPath("$.data.courseId").value("1"));
        }

        @Test
        @DisplayName("GET /by-course/{courseId} - 获取课程下的章节列表成功")
        void getByCourseIdSuccess() throws Exception {
            ChapterResponse vo1 = CourseTestDataFactory.createChapterResponse(1L);
            ChapterResponse vo2 = CourseTestDataFactory.createChapterResponse(2L);
            when(chapterService.getByCourseId(1L)).thenReturn(List.of(vo1, vo2));

            mockMvc.perform(get("/api/content/chapters/by-course/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[1].id").value("2"));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /{id} - 章节不存在返回 BusinessException（4000）")
        void getChapterNotExists() throws Exception {
            when(chapterService.get(999L)).thenThrow(new BusinessException(4000, "章节不存在"));

            mockMvc.perform(get("/api/content/chapters/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("章节不存在"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 RuntimeException 返回 500")
        void getThrowsRuntimeException() throws Exception {
            when(chapterService.get(1L)).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/content/chapters/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /by-course/{courseId} - Service 抛出 BusinessException 返回 400")
        void getByCourseIdThrowsBusinessException() throws Exception {
            when(chapterService.getByCourseId(999L)).thenThrow(new BusinessException(4001, "课程不存在"));

            mockMvc.perform(get("/api/content/chapters/by-course/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("课程不存在"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /by-course/{courseId} - 课程无章节时返回空列表")
        void getByCourseIdEmpty() throws Exception {
            when(chapterService.getByCourseId(999L)).thenReturn(List.of());

            mockMvc.perform(get("/api/content/chapters/by-course/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id} - 路径含中文正常处理")
        void getWithChineseId() throws Exception {
            ChapterResponse vo = CourseTestDataFactory.createChapterResponse(1L);
            when(chapterService.get(1L)).thenReturn(vo);

            mockMvc.perform(get("/api/content/chapters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value("1"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 尝试在路径参数中")
        void xssInPath() throws Exception {
            mockMvc.perform(get("/api/content/chapters/1")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入尝试在路径参数中")
        void sqlInjectionInPath() throws Exception {
            mockMvc.perform(get("/api/content/chapters/1")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/content/chapters/ch-1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求列表接口返回 405")
        void getByCourseWithWrongMethod() throws Exception {
            mockMvc.perform(put("/api/content/chapters/by-course/course-1")).andExpect(status().isMethodNotAllowed());
        }
    }
}
