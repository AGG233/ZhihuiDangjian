package com.rauio.smartdangjian.controller.user;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.controller.user.UserChapterController;
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserChapterControllerTest.TestConfig.class
)
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
            ChapterVO vo = CourseTestDataFactory.createChapterVO("ch-1");
            when(chapterService.get("ch-1")).thenReturn(vo);

            mockMvc.perform(get("/api/content/chapters/ch-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("ch-1"))
                    .andExpect(jsonPath("$.data.title").value("test-chapter"))
                    .andExpect(jsonPath("$.data.description").value("test-chapter-description"))
                    .andExpect(jsonPath("$.data.orderIndex").value(1))
                    .andExpect(jsonPath("$.data.courseId").value("course-1"));
        }

        @Test
        @DisplayName("GET /by-course/{courseId} - 获取课程下的章节列表成功")
        void getByCourseIdSuccess() throws Exception {
            ChapterVO vo1 = CourseTestDataFactory.createChapterVO("ch-1");
            ChapterVO vo2 = CourseTestDataFactory.createChapterVO("ch-2");
            when(chapterService.getByCourseId("course-1")).thenReturn(List.of(vo1, vo2));

            mockMvc.perform(get("/api/content/chapters/by-course/course-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value("ch-1"))
                    .andExpect(jsonPath("$.data[1].id").value("ch-2"));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /{id} - 章节不存在返回 BusinessException（4000）")
        void getChapterNotExists() throws Exception {
            when(chapterService.get("nonexistent"))
                    .thenThrow(new BusinessException(4000, "章节不存在"));

            mockMvc.perform(get("/api/content/chapters/nonexistent"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("章节不存在"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 RuntimeException 返回 500")
        void getThrowsRuntimeException() throws Exception {
            when(chapterService.get("ch-1"))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/content/chapters/ch-1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /by-course/{courseId} - Service 抛出 BusinessException 返回 400")
        void getByCourseIdThrowsBusinessException() throws Exception {
            when(chapterService.getByCourseId("invalid-course"))
                    .thenThrow(new BusinessException(4001, "课程不存在"));

            mockMvc.perform(get("/api/content/chapters/by-course/invalid-course"))
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
            when(chapterService.getByCourseId("empty-course")).thenReturn(List.of());

            mockMvc.perform(get("/api/content/chapters/by-course/empty-course"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id} - 路径含中文正常处理")
        void getWithChineseId() throws Exception {
            ChapterVO vo = CourseTestDataFactory.createChapterVO("ch-1");
            when(chapterService.get("第一章")).thenReturn(vo);

            mockMvc.perform(get("/api/content/chapters/第一章"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("ch-1"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 尝试在路径参数中")
        void xssInPath() throws Exception {
            when(chapterService.get("<script>alert('xss')</script>")).thenReturn(null);

            mockMvc.perform(get("/api/content/chapters/%3Cscript%3Ealert('xss')%3C%2Fscript%3E"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入尝试在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(chapterService.get("' OR '1'='1")).thenReturn(null);

            mockMvc.perform(get("/api/content/chapters/{id}", "' OR '1'='1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/content/chapters/ch-1"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求列表接口返回 405")
        void getByCourseWithWrongMethod() throws Exception {
            mockMvc.perform(put("/api/content/chapters/by-course/course-1"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
