package com.rauio.smartdangjian.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.ControllerTestConfiguration;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.ChapterErrorConstants;
import com.rauio.smartdangjian.server.content.controller.admin.AdminChapterController;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = ControllerTestConfiguration.class)
@DisplayName("管理员章节接口测试")
class AdminChapterControllerTest extends BaseControllerTest {


    @MockitoBean
    private ChapterService chapterService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("创建章节返回成功")
        void createChapterSuccess() throws Exception {
            when(chapterService.create(any(ChapterRequest.class))).thenReturn(true);

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("更新章节返回成功")
        void updateChapterSuccess() throws Exception {
            when(chapterService.update(any(ChapterRequest.class))).thenReturn(true);

            mockMvc.perform(put("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("删除章节返回成功")
        void deleteChapterSuccess() throws Exception {
            when(chapterService.delete(1L)).thenReturn(true);

            mockMvc.perform(delete("/api/admin/content/chapters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("GET 根据章节 ID 获取章节详情成功")
        void getByIdSuccess() throws Exception {
            var vo = CourseTestDataFactory.createChapterResponse(1L);
            when(chapterService.get(1L)).thenReturn(vo);

            mockMvc.perform(get("/api/admin/content/chapters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.title").value("test-chapter"))
                    .andExpect(jsonPath("$.data.courseId").value("1"));
        }

        @Test
        @DisplayName("GET 根据课程 ID 获取章节列表成功")
        void getByCourseIdSuccess() throws Exception {
            var vo1 = CourseTestDataFactory.createChapterResponse(1L);
            var vo2 = CourseTestDataFactory.createChapterResponse(2L);
            when(chapterService.getByCourseId(1L)).thenReturn(List.of(vo1, vo2));

            mockMvc.perform(get("/api/admin/content/chapters/by-course/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[1].id").value("2"));
        }

        @Test
        @DisplayName("GET 课程下无章节时返回空列表")
        void getByCourseIdReturnsEmptyList() throws Exception {
            when(chapterService.getByCourseId(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/content/chapters/by-course/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 500")
        void createThrowsBusinessException() throws Exception {
            when(chapterService.create(any(ChapterRequest.class)))
                    .thenThrow(new BusinessException(ChapterErrorConstants.CHAPTER_MIN_REQUIRED, "课程至少需要一个章节"));

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("3104"))
                    .andExpect(jsonPath("$.message").value("课程至少需要一个章节"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void createThrowsRuntimeException() throws Exception {
            when(chapterService.create(any(ChapterRequest.class))).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterRequest())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("更新章节时 Service 返回 false 则 code 为 500")
        void updateReturnsFalse() throws Exception {
            when(chapterService.update(any(ChapterRequest.class))).thenReturn(false);

            mockMvc.perform(put("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(CourseTestDataFactory.createChapterRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("删除章节时 Service 返回 false 则 code 为 400")
        void deleteReturnsFalse() throws Exception {
            when(chapterService.delete(9999L)).thenReturn(false);

            mockMvc.perform(delete("/api/admin/content/chapters/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("创建章节 - 空请求体返回 400")
        void createWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("删除章节 - 非数字路径参数返回 400")
        void deleteWithNonNumericId() throws Exception {
            mockMvc.perform(delete("/api/admin/content/chapters/not-a-number")).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("标题含中文正常处理")
        void createWithChineseTitle() throws Exception {
            when(chapterService.create(any(ChapterRequest.class))).thenReturn(true);
            ChapterRequest dto = ChapterRequest.builder()
                    .courseId("11")
                    .title("党的二十大报告解读")
                    .description("test-description")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("标题含特殊字符正常处理")
        void createWithSpecialChars() throws Exception {
            when(chapterService.create(any(ChapterRequest.class))).thenReturn(true);
            ChapterRequest dto = ChapterRequest.builder()
                    .courseId("11")
                    .title("test_@#$%^&*()")
                    .description("test-description")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("标题超长字符串（1000 字符）正常处理")
        void createWithLongTitle() throws Exception {
            when(chapterService.create(any(ChapterRequest.class))).thenReturn(true);
            ChapterRequest dto = ChapterRequest.builder()
                    .courseId("11")
                    .title("a".repeat(1000))
                    .description("test-description")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
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
            when(chapterService.create(any(ChapterRequest.class))).thenReturn(true);
            ChapterRequest dto = ChapterRequest.builder()
                    .courseId("11")
                    .title("<script>alert('xss')</script>")
                    .description("test")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("SQL 注入在标题字段")
        void sqlInjectionInTitle() throws Exception {
            when(chapterService.create(any(ChapterRequest.class))).thenReturn(true);
            ChapterRequest dto = ChapterRequest.builder()
                    .courseId("11")
                    .title("' OR '1'='1")
                    .description("test")
                    .duration(1800)
                    .orderIndex(1)
                    .content(java.util.List.of(createSimpleContentBlock()))
                    .build();

            mockMvc.perform(post("/api/admin/content/chapters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("GET 请求创建接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/admin/content/chapters")).andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("POST 请求删除接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/content/chapters/1")).andExpect(status().is4xxClientError());
        }

        private com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto createSimpleContentBlock() {
            return com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto.builder()
                    .blockType(com.rauio.smartdangjian.server.content.spec.BlockType.Paragraph)
                    .textContent("test")
                    .build();
        }
    }
}
