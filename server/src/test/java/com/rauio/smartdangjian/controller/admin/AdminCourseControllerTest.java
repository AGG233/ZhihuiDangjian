package com.rauio.smartdangjian.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.controller.admin.AdminCourseController;
import com.rauio.smartdangjian.server.content.pojo.dto.CourseDto;
import com.rauio.smartdangjian.server.content.service.course.CourseService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AdminCourseControllerTest.TestConfig.class)
@DisplayName("管理员课程接口测试")
class AdminCourseControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminCourseController adminCourseController(CourseService courseService) {
            return new AdminCourseController(courseService);
        }
    }

    @MockitoBean
    private CourseService courseService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("创建课程返回成功")
        void createCourseSuccess() throws Exception {
            when(courseService.create(any(CourseDto.class))).thenReturn(true);

            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createCourseDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("更新课程返回成功")
        void updateCourseSuccess() throws Exception {
            when(courseService.update(any(CourseDto.class), eq("course-1"))).thenReturn(true);

            mockMvc.perform(put("/api/admin/content/courses/course-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createCourseDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("删除课程返回成功")
        void deleteCourseSuccess() throws Exception {
            when(courseService.delete("course-1")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/content/courses/course-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400 并携带错误码")
        void createThrowsBusinessException() throws Exception {
            when(courseService.create(any(CourseDto.class))).thenThrow(new BusinessException(4000, "课程创建失败"));

            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createCourseDto())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("课程创建失败"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void createThrowsRuntimeException() throws Exception {
            when(courseService.create(any(CourseDto.class))).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createCourseDto())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("更新课程时 Service 返回 false 则 code 为 400")
        void updateReturnsFalse() throws Exception {
            when(courseService.update(any(CourseDto.class), eq("nonexistent"))).thenReturn(false);

            mockMvc.perform(put("/api/admin/content/courses/nonexistent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createCourseDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("删除课程时 Service 返回 false 则 code 为 400")
        void deleteReturnsFalse() throws Exception {
            when(courseService.delete("nonexistent")).thenReturn(false);

            mockMvc.perform(delete("/api/admin/content/courses/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("标题含中文正常处理")
        void createWithChineseTitle() throws Exception {
            when(courseService.create(any(CourseDto.class))).thenReturn(true);
            CourseDto dto = CourseDto.builder()
                    .title("习近平新时代中国特色社会主义思想")
                    .description("test-description")
                    .categoryId("cat-1")
                    .build();

            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("标题含特殊字符正常处理")
        void createWithSpecialChars() throws Exception {
            when(courseService.create(any(CourseDto.class))).thenReturn(true);
            CourseDto dto = CourseDto.builder()
                    .title("test_@#$%^&*()")
                    .description("test-description")
                    .categoryId("cat-1")
                    .build();

            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("标题超长字符串（1000 字符）正常处理")
        void createWithLongTitle() throws Exception {
            when(courseService.create(any(CourseDto.class))).thenReturn(true);
            CourseDto dto = CourseDto.builder()
                    .title("a".repeat(1000))
                    .description("test-description")
                    .categoryId("cat-1")
                    .build();

            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("描述为空字符串时正常处理（description 为可选字段）")
        void createWithEmptyDescription() throws Exception {
            when(courseService.create(any(CourseDto.class))).thenReturn(true);
            CourseDto dto = CourseDto.builder()
                    .title("test-course")
                    .description("")
                    .categoryId("cat-1")
                    .build();

            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在标题字段")
        void xssInTitle() throws Exception {
            when(courseService.create(any(CourseDto.class))).thenReturn(true);
            CourseDto dto = CourseDto.builder()
                    .title("<script>alert('xss')</script>")
                    .description("test")
                    .categoryId("cat-1")
                    .build();

            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在标题字段")
        void sqlInjectionInTitle() throws Exception {
            when(courseService.create(any(CourseDto.class))).thenReturn(true);
            CourseDto dto = CourseDto.builder()
                    .title("' OR '1'='1")
                    .description("test")
                    .categoryId("cat-1")
                    .build();

            mockMvc.perform(post("/api/admin/content/courses/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET 请求创建接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/admin/content/courses/")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("POST 请求删除接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/content/courses/course-1")).andExpect(status().isMethodNotAllowed());
        }
    }
}
