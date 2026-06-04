package com.rauio.smartdangjian.controller.user;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.ControllerTestConfiguration;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.course.pojo.entity.Course;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.course.pojo.response.PageResponse;
import com.rauio.smartdangjian.server.course.service.course.CourseService;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ControllerTestConfiguration.class)
@DisplayName("用户课程接口测试")
class UserCourseControllerTest extends BaseControllerTest {

    @MockitoBean
    private CourseService courseService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /{id} - 获取课程详情成功")
        void getCourseDetailSuccess() throws Exception {
            CourseResponse vo = CourseTestDataFactory.createCourseResponse(1L);
            when(courseService.get(1L)).thenReturn(vo);

            mockMvc.perform(get("/api/content/courses/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("test-course"))
                    .andExpect(jsonPath("$.data.categoryId").value(1))
                    .andExpect(jsonPath("$.data.difficulty").value("easy"))
                    .andExpect(jsonPath("$.data.estimatedDuration").value(60))
                    .andExpect(jsonPath("$.data.creatorId").value(1));
        }

        @Test
        @DisplayName("GET / - 分页获取课程列表成功")
        void getPageSuccess() throws Exception {
            CourseResponse vo = CourseTestDataFactory.createCourseResponse(1L);
            PageResponse<Object> pageVO = CourseTestDataFactory.createPageResponse(List.of(vo), 1, 1, 10);
            when(courseService.getPage(1, 10)).thenReturn(pageVO);

            mockMvc.perform(get("/api/content/courses").param("pageNum", "1").param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.current").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("GET / - 不传分页参数时使用默认值")
        void getPageWithDefaults() throws Exception {
            PageResponse<Object> emptyPage = CourseTestDataFactory.createEmptyPageResponse(1, 10);
            when(courseService.getPage(1, 10)).thenReturn(emptyPage);

            mockMvc.perform(get("/api/content/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.current").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("GET /learned/me - 获取用户已学习课程")
        void getLearnedCoursesSuccess() throws Exception {
            Course course = CourseTestDataFactory.createCourse(1L);
            when(courseService.getByUserId(1L)).thenReturn(List.of(course));

            mockMvc.perform(get("/api/content/courses/learned/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].id").value("1"))
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
            when(courseService.get(999L)).thenReturn(null);

            mockMvc.perform(get("/api/content/courses/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 BusinessException 返回 400")
        void getThrowsBusinessException() throws Exception {
            when(courseService.get(1L)).thenThrow(new BusinessException(4001, "资源不存在"));

            mockMvc.perform(get("/api/content/courses/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("资源不存在"));
        }

        @Test
        @DisplayName("GET / - Service 抛出 RuntimeException 返回 500")
        void getPageThrowsRuntimeException() throws Exception {
            when(courseService.getPage(anyInt(), anyInt())).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/content/courses"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /learned/me - Service 抛出 BusinessException 返回 400")
        void getLearnedThrowsBusinessException() throws Exception {
            when(courseService.getByUserId(1L))
                    .thenThrow(new BusinessException(UserErrorConstants.USER_NOT_EXISTS, "用户不存在"));

            mockMvc.perform(get("/api/content/courses/learned/me"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("2005"))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET / - 空分页结果返回空列表")
        void getPageEmptyResult() throws Exception {
            PageResponse<Object> emptyPage = CourseTestDataFactory.createEmptyPageResponse(1, 10);
            when(courseService.getPage(1, 10)).thenReturn(emptyPage);

            mockMvc.perform(get("/api/content/courses").param("pageNum", "1").param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.total").value(0))
                    .andExpect(jsonPath("$.data.list").isEmpty());
        }

        @Test
        @DisplayName("GET /learned/me - 返回空列表")
        void getLearnedCoursesEmpty() throws Exception {
            when(courseService.getByUserId(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/content/courses/learned/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id} - 路径含中文正常处理")
        void getWithChineseId() throws Exception {
            CourseResponse vo = CourseTestDataFactory.createCourseResponse(1L);
            when(courseService.get(1L)).thenReturn(vo);

            mockMvc.perform(get("/api/content/courses/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @ParameterizedTest(name = "course id={0}")
        @ValueSource(strings = {"<script>alert('xss')", "' OR '1'='1", "3.14", "课程"})
        @DisplayName("非法课程 ID 路径参数返回 400")
        void invalidCourseIdInPathReturns400(String id) throws Exception {
            mockMvc.perform(get("/api/content/courses/{id}", id)).andExpect(status().isBadRequest());
        }

        @ParameterizedTest(name = "{0} {1}")
        @CsvSource({"POST,/api/content/courses/course-1", "DELETE,/api/content/courses"})
        @DisplayName("错误 HTTP 方法返回 405")
        void wrongMethodReturns405(String method, String path) throws Exception {
            if ("POST".equals(method)) {
                mockMvc.perform(post(path)).andExpect(status().isMethodNotAllowed());
            } else if ("DELETE".equals(method)) {
                mockMvc.perform(delete(path)).andExpect(status().isMethodNotAllowed());
            } else {
                throw new IllegalArgumentException("Unsupported method: " + method);
            }
        }

        @Test
        @DisplayName("旧的用户 ID 学习课程路径不可用")
        void oldUserIdLearnedCoursesPathIsUnavailable() throws Exception {
            mockMvc.perform(get("/api/content/courses/learned/2")).andExpect(status().isNotFound());
        }
    }
}
