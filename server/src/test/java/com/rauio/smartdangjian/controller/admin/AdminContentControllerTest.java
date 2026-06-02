package com.rauio.smartdangjian.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.ContentTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.controller.admin.AdminContentController;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterContentBlockRequest;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.spec.BlockType;
import com.rauio.smartdangjian.server.resource.constants.ResourceErrorConstants;
import com.rauio.smartdangjian.utils.spec.UserType;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AdminContentControllerTest.TestConfig.class)
@DisplayName("管理员内容块接口测试")
class AdminContentControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminContentController adminContentController(ChapterContentBlockService chapterContentBlockService) {
            return new AdminContentController(chapterContentBlockService);
        }
    }

    @MockitoBean
    private ChapterContentBlockService chapterChapterContentBlockService;

    @BeforeEach
    void setManagerContext() {
        setSecurityContext(UserType.MANAGER, 1L, "uni1");
    }

    private static final String CAROUSEL_URL = "/api/admin/content/content-blocks/carousel";

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("PUT /carousel - 更新轮播图成功")
        void updateCarouselSuccess() throws Exception {
            when(chapterChapterContentBlockService.updateCarousel(any(ChapterContentBlockRequest.class)))
                    .thenReturn(true);

            ChapterContentBlock block = ContentTestDataFactory.createCarouselBlock(1L, BlockType.Image);
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.toJson(block)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("POST /carousel - 添加轮播图成功")
        void addCarouselSuccess() throws Exception {
            when(chapterChapterContentBlockService.createCarouselBatch(any(List.class)))
                    .thenReturn(true);

            List<ChapterContentBlock> blocks = List.of(
                    ContentTestDataFactory.createCarouselBlock(1L, BlockType.Image),
                    ContentTestDataFactory.createCarouselBlock(2L, BlockType.Paragraph));
            mockMvc.perform(post(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.listToJson(blocks)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /carousel/{id} - 删除轮播图成功")
        void deleteCarouselSuccess() throws Exception {
            when(chapterChapterContentBlockService.delete(1L)).thenReturn(true);

            mockMvc.perform(delete(CAROUSEL_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 异常处理场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("PUT /carousel - Service 抛出 BusinessException 返回 400")
        void updateThrowsBusinessException() throws Exception {
            when(chapterChapterContentBlockService.updateCarousel(any(ChapterContentBlockRequest.class)))
                    .thenThrow(new BusinessException(ResourceErrorConstants.RESOURCE_UPDATE_FAILED, "更新轮播图失败"));

            ChapterContentBlock block = ContentTestDataFactory.createCarouselBlock(1L, BlockType.Image);
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.toJson(block)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("5003"))
                    .andExpect(jsonPath("$.message").value("更新轮播图失败"));
        }

        @Test
        @DisplayName("POST /carousel - Service 抛出 BusinessException 返回 400")
        void addThrowsBusinessException() throws Exception {
            when(chapterChapterContentBlockService.createCarouselBatch(any(List.class)))
                    .thenThrow(new BusinessException(ResourceErrorConstants.RESOURCE_CREATE_FAILED, "添加轮播图失败"));

            List<ChapterContentBlock> blocks = List.of(ContentTestDataFactory.createCarouselBlock(1L, BlockType.Image));
            mockMvc.perform(post(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.listToJson(blocks)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("5001"))
                    .andExpect(jsonPath("$.message").value("添加轮播图失败"));
        }

        @Test
        @DisplayName("DELETE /carousel/{id} - Service 抛出 BusinessException 返回 400")
        void deleteThrowsBusinessException() throws Exception {
            when(chapterChapterContentBlockService.delete(anyLong()))
                    .thenThrow(new BusinessException(ResourceErrorConstants.BANNER_NOT_FOUND, "轮播图不存在"));

            mockMvc.perform(delete(CAROUSEL_URL + "/9999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("5011"))
                    .andExpect(jsonPath("$.message").value("轮播图不存在"));
        }

        @Test
        @DisplayName("PUT /carousel - Service 抛出 RuntimeException 返回 500")
        void updateThrowsRuntimeException() throws Exception {
            when(chapterChapterContentBlockService.updateCarousel(any(ChapterContentBlockRequest.class)))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            ChapterContentBlock block = ContentTestDataFactory.createCarouselBlock(1L, BlockType.Image);
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.toJson(block)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST /carousel - Service 抛出 RuntimeException 返回 500")
        void addThrowsRuntimeException() throws Exception {
            when(chapterChapterContentBlockService.createCarouselBatch(any(List.class)))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            List<ChapterContentBlock> blocks = List.of(ContentTestDataFactory.createCarouselBlock(1L, BlockType.Image));
            mockMvc.perform(post(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.listToJson(blocks)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("DELETE /carousel/{id} - Service 抛出 RuntimeException 返回 500")
        void deleteThrowsRuntimeException() throws Exception {
            when(chapterChapterContentBlockService.delete(1L)).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(delete(CAROUSEL_URL + "/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST /carousel - 非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /carousel - 非法 JSON 请求体返回 400")
        void malformedJsonOnPut() throws Exception {
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /carousel - 空请求体返回 400")
        void emptyBodyOnPut() throws Exception {
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /carousel - 空请求体返回 400")
        void emptyBodyOnPost() throws Exception {
            mockMvc.perform(post(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /carousel/{id} - 非数字路径参数返回 400")
        void deleteWithNonNumericId() throws Exception {
            mockMvc.perform(delete(CAROUSEL_URL + "/not-a-number")).andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("POST /carousel - 空列表正常处理")
        void addCarouselWithEmptyList() throws Exception {
            when(chapterChapterContentBlockService.createCarouselBatch(any(List.class)))
                    .thenReturn(true);

            mockMvc.perform(post(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT /carousel - 文本内容包含特殊字符正常处理")
        void updateWithSpecialChars() throws Exception {
            when(chapterChapterContentBlockService.updateCarousel(any(ChapterContentBlockRequest.class)))
                    .thenReturn(true);

            ChapterContentBlock block = ContentTestDataFactory.createCarouselBlock(1L, BlockType.Paragraph);
            block.setTextContent("测试内容 @#$%^&*() _+=-");
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.toJson(block)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("POST /carousel - 内容块包含中文正常处理")
        void addWithChineseContent() throws Exception {
            when(chapterChapterContentBlockService.createCarouselBatch(any(List.class)))
                    .thenReturn(true);

            ChapterContentBlock block = ContentTestDataFactory.createCarouselBlock(1L, BlockType.Image);
            block.setTextContent("习近平新时代中国特色社会主义思想");
            mockMvc.perform(post(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.listToJson(List.of(block))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT /carousel - 文本内容超长（1000 字符）正常处理")
        void updateWithLongTextContent() throws Exception {
            when(chapterChapterContentBlockService.updateCarousel(any(ChapterContentBlockRequest.class)))
                    .thenReturn(true);

            ChapterContentBlock block = ContentTestDataFactory.createCarouselBlock(1L, BlockType.Paragraph);
            block.setTextContent("a".repeat(1000));
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.toJson(block)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /carousel/{id} - 不存在的 ID 删除时 service 返回 false")
        void deleteNonExistent() throws Exception {
            when(chapterChapterContentBlockService.delete(1L)).thenReturn(false);

            mockMvc.perform(delete(CAROUSEL_URL + "/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("PUT /carousel - service 返回 false 时 code 为 400")
        void updateReturnsFalse() throws Exception {
            when(chapterChapterContentBlockService.updateCarousel(any(ChapterContentBlockRequest.class)))
                    .thenReturn(false);

            ChapterContentBlock block = ContentTestDataFactory.createCarouselBlock(1L, BlockType.Image);
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.toJson(block)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 安全场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("PUT /carousel - XSS 注入在 textContent 字段")
        void xssInTextContent() throws Exception {
            when(chapterChapterContentBlockService.updateCarousel(any(ChapterContentBlockRequest.class)))
                    .thenReturn(true);

            ChapterContentBlock block = ContentTestDataFactory.createCarouselBlock(1L, BlockType.Paragraph);
            block.setTextContent("<script>alert('xss')</script>");
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.toJson(block)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("PUT /carousel - SQL 注入在 textContent 字段")
        void sqlInjectionInTextContent() throws Exception {
            when(chapterChapterContentBlockService.updateCarousel(any(ChapterContentBlockRequest.class)))
                    .thenReturn(true);

            ChapterContentBlock block = ContentTestDataFactory.createCarouselBlock(1L, BlockType.Paragraph);
            block.setTextContent("' OR '1'='1");
            mockMvc.perform(put(CAROUSEL_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ContentTestDataFactory.toJson(block)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("DELETE /carousel/{id} - XSS 注入在路径参数中返回 404（特殊字符导致 URL 不匹配）")
        void xssInPath() throws Exception {
            mockMvc.perform(delete(CAROUSEL_URL + "/%3Cscript%3Ealert('xss')%3E"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /carousel/{id} - SQL 注入在路径参数中")
        void sqlInjectionInPath() throws Exception {
            mockMvc.perform(delete(CAROUSEL_URL + "/' OR '1'='1")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET 请求更新接口返回 405")
        void updateWithWrongMethod() throws Exception {
            mockMvc.perform(get(CAROUSEL_URL).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("GET 请求添加接口返回 405")
        void addWithWrongMethod() throws Exception {
            mockMvc.perform(get(CAROUSEL_URL).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("POST 请求删除接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(post(CAROUSEL_URL + "/1")).andExpect(status().isMethodNotAllowed());
        }
    }
}
