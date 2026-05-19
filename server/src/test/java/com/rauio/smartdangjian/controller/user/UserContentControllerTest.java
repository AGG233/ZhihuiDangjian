package com.rauio.smartdangjian.controller.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.rauio.smartdangjian.controller.factory.ContentTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.controller.user.UserContentController;
import com.rauio.smartdangjian.server.content.pojo.vo.ContentBlockVO;
import com.rauio.smartdangjian.server.content.service.ContentBlockService;
import com.rauio.smartdangjian.server.content.spec.BlockType;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserContentControllerTest.TestConfig.class)
@DisplayName("用户内容块接口测试")
class UserContentControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserContentController userContentController(ContentBlockService contentBlockService) {
            return new UserContentController(contentBlockService);
        }
    }

    @MockitoBean
    private ContentBlockService contentBlockService;

    private static final String CAROUSEL_URL = "/api/content/content-blocks/carousel";

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /carousel - 获取轮播图列表成功")
        void getCarouselSuccess() throws Exception {
            List<ContentBlockVO> voList = ContentTestDataFactory.createContentBlockVOList(3);
            when(contentBlockService.getByParentId("1145141919810")).thenReturn(voList);

            mockMvc.perform(get(CAROUSEL_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(3));
        }

        @Test
        @DisplayName("GET /carousel - 返回的 VO 包含正确字段")
        void getCarouselContainsAllFields() throws Exception {
            ContentBlockVO vo = ContentTestDataFactory.createCarouselVO("1145141919810", BlockType.Image);
            when(contentBlockService.getByParentId("1145141919810")).thenReturn(List.of(vo));

            mockMvc.perform(get(CAROUSEL_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 异常处理场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400")
        void getCarouselThrowsBusinessException() throws Exception {
            when(contentBlockService.getByParentId("1145141919810")).thenThrow(new BusinessException(4000, "轮播图查询失败"));

            mockMvc.perform(get(CAROUSEL_URL))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("轮播图查询失败"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void getCarouselThrowsRuntimeException() throws Exception {
            when(contentBlockService.getByParentId("1145141919810")).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get(CAROUSEL_URL))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("Service 返回 null 时正常处理")
        void getCarouselReturnsNull() throws Exception {
            when(contentBlockService.getByParentId("1145141919810")).thenReturn(null);

            mockMvc.perform(get(CAROUSEL_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("轮播图为空时返回空列表")
        void getCarouselEmpty() throws Exception {
            when(contentBlockService.getByParentId("1145141919810")).thenReturn(List.of());

            mockMvc.perform(get(CAROUSEL_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("大量轮播图（10 个）正常返回")
        void getCarouselWithManyItems() throws Exception {
            List<ContentBlockVO> voList = ContentTestDataFactory.createContentBlockVOList(10);
            when(contentBlockService.getByParentId("1145141919810")).thenReturn(voList);

            mockMvc.perform(get(CAROUSEL_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(10));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 安全场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("STUDENT 用户可正常访问 GET /carousel")
        void studentCanAccessCarousel() throws Exception {
            // Default context is SCHOOL; no @PermissionAccess on UserContentController
            List<ContentBlockVO> voList = ContentTestDataFactory.createContentBlockVOList(1);
            when(contentBlockService.getByParentId("1145141919810")).thenReturn(voList);

            mockMvc.perform(get(CAROUSEL_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post(CAROUSEL_URL)).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求获取接口返回 405")
        void getWithPutMethod() throws Exception {
            mockMvc.perform(put(CAROUSEL_URL)).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求获取接口返回 405")
        void getWithDeleteMethod() throws Exception {
            mockMvc.perform(delete(CAROUSEL_URL)).andExpect(status().isMethodNotAllowed());
        }
    }
}
