package com.rauio.smartdangjian.controller.admin;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.BannerTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.controller.admin.AdminBannerController;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.service.BannerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AdminBannerControllerTest.TestConfig.class
)
@DisplayName("管理员轮播图接口测试")
class AdminBannerControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminBannerController adminBannerController(BannerService bannerService) {
            return new AdminBannerController(bannerService);
        }
    }

    @MockitoBean
    private BannerService bannerService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET / - 获取轮播图列表成功")
        void listBannersSuccess() throws Exception {
            List<ResourceMeta> banners = List.of(
                    BannerTestDataFactory.createResourceMeta("r-1"),
                    BannerTestDataFactory.createResourceMeta("r-2")
            );
            when(bannerService.getList()).thenReturn(banners);

            mockMvc.perform(get("/api/admin/resource/banners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("GET /{order} - 获取单个轮播图成功")
        void getBannerSuccess() throws Exception {
            when(bannerService.get(0)).thenReturn(BannerTestDataFactory.createResourceMeta("r-1"));

            mockMvc.perform(get("/api/admin/resource/banners/0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("r-1"));
        }

        @Test
        @DisplayName("POST / - 创建轮播图成功")
        void createBannerSuccess() throws Exception {
            when(bannerService.create(any(String.class))).thenReturn(BannerTestDataFactory.createResourceMeta("r-1"));

            mockMvc.perform(post("/api/admin/resource/banners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BannerTestDataFactory.toJson(BannerTestDataFactory.createBannerCreateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("r-1"));
        }

        @Test
        @DisplayName("PUT /{order} - 更新轮播图成功")
        void updateBannerSuccess() throws Exception {
            when(bannerService.update(anyInt(), any(String.class)))
                    .thenReturn(BannerTestDataFactory.createResourceMeta("r-1"));

            mockMvc.perform(put("/api/admin/resource/banners/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BannerTestDataFactory.toJson(BannerTestDataFactory.createBannerUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("r-1"));
        }

        @Test
        @DisplayName("DELETE /{order} - 删除轮播图成功")
        void deleteBannerSuccess() throws Exception {
            when(bannerService.delete(0)).thenReturn(true);

            mockMvc.perform(delete("/api/admin/resource/banners/0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400")
        void serviceThrowsBusinessException() throws Exception {
            when(bannerService.get(0)).thenThrow(new BusinessException(4000, "轮播图不存在"));

            mockMvc.perform(get("/api/admin/resource/banners/0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("轮播图不存在"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(bannerService.getList()).thenThrow(new RuntimeException("Redis连接失败"));

            mockMvc.perform(get("/api/admin/resource/banners"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - 请求体为空返回 400")
        void createWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/admin/resource/banners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/resource/banners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /{order} - 请求体为空返回 400")
        void updateWithEmptyBody() throws Exception {
            mockMvc.perform(put("/api/admin/resource/banners/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }
}

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET / - 空列表返回空数组")
        void listBannersEmpty() throws Exception {
            when(bannerService.getList()).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/resource/banners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("POST / - 通过 hash 创建轮播图")
        void createBannerByHash() throws Exception {
            when(bannerService.createByHash(any(String.class)))
                    .thenReturn(BannerTestDataFactory.createResourceMeta("r-1"));

            mockMvc.perform(post("/api/admin/resource/banners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BannerTestDataFactory.toJson(BannerTestDataFactory.createBannerCreateRequestByHash())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("PUT /{order} - 通过 hash 更新轮播图")
        void updateBannerByHash() throws Exception {
            when(bannerService.updateByHash(anyInt(), any(String.class)))
                    .thenReturn(BannerTestDataFactory.createResourceMeta("r-1"));

            mockMvc.perform(put("/api/admin/resource/banners/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BannerTestDataFactory.toJson(BannerTestDataFactory.createBannerUpdateRequestByHash())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在 resourceId 字段")
        void xssInResourceId() throws Exception {
            when(bannerService.create(any(String.class)))
                    .thenReturn(BannerTestDataFactory.createResourceMeta("r-1"));

            String json = "{\"resourceId\": \"<script>alert('xss')</script>\"}";
            mockMvc.perform(post("/api/admin/resource/banners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在 resourceId 字段")
        void sqlInjectionInResourceId() throws Exception {
            when(bannerService.create(any(String.class)))
                    .thenReturn(BannerTestDataFactory.createResourceMeta("r-1"));

            String json = "{\"resourceId\": \"' OR '1'='1\"}";
            mockMvc.perform(post("/api/admin/resource/banners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET 请求创建接口正常处理（匹配查询接口）")
        void createWithWrongMethod() throws Exception {
            when(bannerService.getList()).thenReturn(java.util.List.of());
            mockMvc.perform(get("/api/admin/resource/banners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST 请求获取单个接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/resource/banners/0"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
