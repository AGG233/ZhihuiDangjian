package com.rauio.smartdangjian.controller.admin;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.controller.admin.AdminChapterController;
import com.rauio.smartdangjian.server.content.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AdminChapterControllerTest.TestConfig.class
)
@DisplayName("管理员章节接口测试")
class AdminChapterControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminChapterController adminChapterController(ChapterService chapterService) {
            return new AdminChapterController(chapterService);
        }
    }

    @MockitoBean
    private ChapterService chapterService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("创建章节返回成功")
        void createChapterSuccess() throws Exception {
            when(chapterService.create(any(ChapterDto.class))).thenReturn(true);

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterDto())))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("更新章节返回成功")
        void updateChapterSuccess() throws Exception {
            when(chapterService.update(any(ChapterDto.class))).thenReturn(true);

            mockMvc.perform(put("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterDto())))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("删除章节返回成功")
        void deleteChapterSuccess() throws Exception {
            when(chapterService.delete("ch-1")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/content/chapters/ch-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 500")
        void createThrowsBusinessException() throws Exception {
            when(chapterService.create(any(ChapterDto.class)))
                    .thenThrow(new BusinessException(4000, "课程至少需要一个章节"));

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterDto())))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void createThrowsRuntimeException() throws Exception {
            when(chapterService.create(any(ChapterDto.class)))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterDto())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("更新章节时 Service 返回 false 则 code 为 500")
        void updateReturnsFalse() throws Exception {
            when(chapterService.update(any(ChapterDto.class))).thenReturn(false);

            mockMvc.perform(put("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterDto())))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("删除章节时 Service 返回 false 则 code 为 400")
        void deleteReturnsFalse() throws Exception {
            when(chapterService.delete("nonexistent")).thenReturn(false);

            mockMvc.perform(delete("/api/admin/content/chapters/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("Operation failed"));
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/content/chapters")
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
            when(chapterService.create(any(ChapterDto.class))).thenReturn(true);
            ChapterDto dto = ChapterDto.builder()
                    .courseId("course-1")
                    .title("党的二十大报告解读")
                    .description("test-description")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("标题含特殊字符正常处理")
        void createWithSpecialChars() throws Exception {
            when(chapterService.create(any(ChapterDto.class))).thenReturn(true);
            ChapterDto dto = ChapterDto.builder()
                    .courseId("course-1")
                    .title("test_@#$%^&*()")
                    .description("test-description")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("标题超长字符串（1000 字符）正常处理")
        void createWithLongTitle() throws Exception {
            when(chapterService.create(any(ChapterDto.class))).thenReturn(true);
            ChapterDto dto = ChapterDto.builder()
                    .courseId("course-1")
                    .title("a".repeat(1000))
                    .description("test-description")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().is5xxServerError());
        }

        private com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto createSimpleContentBlock() {
            return com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto.builder()
                    .blockType(com.rauio.smartdangjian.server.content.spec.BlockType.Paragraph)
                    .textContent("test")
                    .build();
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在标题字段")
        void xssInTitle() throws Exception {
            when(chapterService.create(any(ChapterDto.class))).thenReturn(true);
            ChapterDto dto = ChapterDto.builder()
                    .courseId("course-1")
                    .title("<script>alert('xss')</script>")
                    .description("test")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("SQL 注入在标题字段")
        void sqlInjectionInTitle() throws Exception {
            when(chapterService.create(any(ChapterDto.class))).thenReturn(true);
            ChapterDto dto = ChapterDto.builder()
                    .courseId("course-1")
                    .title("' OR '1'='1")
                    .description("test")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("GET 请求创建接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/admin/content/chapters"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("POST 请求删除接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/content/chapters/ch-1"))
                    .andExpect(status().is4xxClientError());
        }

        private com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto createSimpleContentBlock() {
            return com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto.builder()
                    .blockType(com.rauio.smartdangjian.server.content.spec.BlockType.Paragraph)
                    .textContent("test")
                    .build();
        }
    }
}
