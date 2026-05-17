package com.rauio.smartdangjian.controller.user;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.controller.user.UserCourseController;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.server.content.pojo.vo.PageVO;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserCourseControllerTest.TestConfig.class
)
@DisplayName("用户课程接口测试")
class UserCourseControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserCourseController userCourseController(CourseService courseService) {
            return new UserCourseController(courseService);
        }
    }

    @MockitoBean
    private CourseService courseService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /{id} - 获取课程详情成功")
        void getCourseDetailSuccess() throws Exception {
            CourseVO vo = CourseTestDataFactory.createCourseVO("course-1");
            when(courseService.get("course-1")).thenReturn(vo);

            mockMvc.perform(get("/api/content/courses/course-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("course-1"))
                    .andExpect(jsonPath("$.data.title").value("test-course"))
                    .andExpect(jsonPath("$.data.categoryId").value("cat-1"))
                    .andExpect(jsonPath("$.data.difficulty").value("easy"))
                    .andExpect(jsonPath("$.data.estimatedDuration").value(60))
                    .andExpect(jsonPath("$.data.creatorId").value("admin1"));
        }

        @Test
        @DisplayName("GET / - 分页获取课程列表成功")
        void getPageSuccess() throws Exception {
            CourseVO vo = CourseTestDataFactory.createCourseVO("course-1");
            PageVO<Object> pageVO = CourseTestDataFactory.createPageVO(List.of(vo), 1, 1, 10);
            when(courseService.getPage(1, 10)).thenReturn(pageVO);

            mockMvc.perform(get("/api/content/courses")
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.current").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("GET / - 不传分页参数时使用默认值")
        void getPageWithDefaults() throws Exception {
            PageVO<Object> emptyPage = CourseTestDataFactory.createEmptyPageVO(1, 10);
            when(courseService.getPage(1, 10)).thenReturn(emptyPage);

            mockMvc.perform(get("/api/content/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.current").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("GET /learned/{id} - 获取用户已学习课程")
        void getLearnedCoursesSuccess() throws Exception {
            Course course = CourseTestDataFactory.createCourse("course-1");
            when(courseService.getByUserId("user-1")).thenReturn(List.of(course));

            mockMvc.perform(get("/api/content/courses/learned/user-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].id").value("course-1"))
                    .andExpect(jsonPath("$.data[0].title").value("test-course"))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /{id} - 服务返回 null 时 code 为 400")
        void getReturnsNull() throws Exception {
            when(courseService.get("nonexistent")).thenReturn(null);

            mockMvc.perform(get("/api/content/courses/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 BusinessException 返回 400")
        void getThrowsBusinessException() throws Exception {
            when(courseService.get("course-1"))
                    .thenThrow(new BusinessException(4001, "资源不存在"));

            mockMvc.perform(get("/api/content/courses/course-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("资源不存在"));
        }

        @Test
        @DisplayName("GET / - Service 抛出 RuntimeException 返回 500")
        void getPageThrowsRuntimeException() throws Exception {
            when(courseService.getPage(anyInt(), anyInt()))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/content/courses"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /learned/{id} - Service 抛出 BusinessException 返回 400")
        void getLearnedThrowsBusinessException() throws Exception {
            when(courseService.getByUserId("user-1"))
                    .thenThrow(new BusinessException(4000, "用户不存在"));

            mockMvc.perform(get("/api/content/courses/learned/user-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET / - 空分页结果返回空列表")
        void getPageEmptyResult() throws Exception {
            PageVO<Object> emptyPage = CourseTestDataFactory.createEmptyPageVO(1, 10);
            when(courseService.getPage(1, 10)).thenReturn(emptyPage);

            mockMvc.perform(get("/api/content/courses")
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.total").value(0))
                    .andExpect(jsonPath("$.data.list").isEmpty());
        }

        @Test
        @DisplayName("GET /learned/{id} - 返回空列表")
        void getLearnedCoursesEmpty() throws Exception {
            when(courseService.getByUserId("user-empty")).thenReturn(List.of());

            mockMvc.perform(get("/api/content/courses/learned/user-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id} - 路径含中文正常处理")
        void getWithChineseId() throws Exception {
            CourseVO vo = CourseTestDataFactory.createCourseVO("c-1");
            when(courseService.get("课程")).thenReturn(vo);

            mockMvc.perform(get("/api/content/courses/课程"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 尝试在路径参数中")
        void xssInPath() throws Exception {
            when(courseService.get("<script>alert('xss')</script>")).thenReturn(null);

            mockMvc.perform(get("/api/content/courses/%3Cscript%3Ealert('xss')%3C%2Fscript%3E"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入尝试在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(courseService.get("' OR '1'='1")).thenReturn(null);

            mockMvc.perform(get("/api/content/courses/{id}", "' OR '1'='1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/content/courses/course-1"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求分页接口返回 405")
        void getPageWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/content/courses"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
