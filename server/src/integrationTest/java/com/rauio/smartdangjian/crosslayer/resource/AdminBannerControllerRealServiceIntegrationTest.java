package com.rauio.smartdangjian.crosslayer.resource;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
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
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.resource.controller.admin.AdminBannerController;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.service.BannerService;

@SpringBootTest(classes = AdminBannerControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("管理员轮播图控制层集成测试")
class AdminBannerControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private BannerService bannerService;

    @BeforeEach
    void setUp() {
        reset(bannerService);
        setManagerContext(1L, null);
    }

    @Test
    @DisplayName("GET /api/admin/resource/banners 成功返回轮播图列表")
    void listBanners() throws Exception {
        ResourceMeta meta = ResourceMeta.builder()
                .id(1L)
                .originalName("banner.png")
                .hash("hash123")
                .build();
        when(bannerService.getList()).thenReturn(List.of(meta));

        mockMvc.perform(get("/api/admin/resource/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].originalName").value("banner.png"));

        verify(bannerService).getList();
    }

    @Test
    @DisplayName("GET /api/admin/resource/banners/{order} 成功返回单个轮播图")
    void getBannerByOrder() throws Exception {
        ResourceMeta meta =
                ResourceMeta.builder().id(2L).originalName("slide2.png").build();
        when(bannerService.get(1)).thenReturn(meta);

        mockMvc.perform(get("/api/admin/resource/banners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.originalName").value("slide2.png"));

        verify(bannerService).get(1);
    }

    @Test
    @DisplayName("POST /api/admin/resource/banners 通过 resourceId 创建轮播图")
    void createBannerByResourceId() throws Exception {
        ResourceMeta meta =
                ResourceMeta.builder().id(3L).originalName("new-banner.png").build();
        when(bannerService.create("3")).thenReturn(meta);

        mockMvc.perform(post("/api/admin/resource/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceId\":\"3\",\"hash\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.originalName").value("new-banner.png"));

        verify(bannerService).create("3");
    }

    @Test
    @DisplayName("POST /api/admin/resource/banners 通过 hash 创建轮播图")
    void createBannerByHash() throws Exception {
        ResourceMeta meta =
                ResourceMeta.builder().id(4L).originalName("hash-banner.png").build();
        when(bannerService.createByHash("hash-xx")).thenReturn(meta);

        mockMvc.perform(post("/api/admin/resource/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceId\":\"\",\"hash\":\"hash-xx\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.originalName").value("hash-banner.png"));

        verify(bannerService).createByHash("hash-xx");
    }

    @Test
    @DisplayName("PUT /api/admin/resource/banners/{order} 通过 resourceId 更新轮播图")
    void updateBannerByResourceId() throws Exception {
        ResourceMeta meta =
                ResourceMeta.builder().id(5L).originalName("updated.png").build();
        when(bannerService.update(0, "5")).thenReturn(meta);

        mockMvc.perform(put("/api/admin/resource/banners/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceId\":\"5\",\"hash\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.originalName").value("updated.png"));

        verify(bannerService).update(0, "5");
    }

    @Test
    @DisplayName("DELETE /api/admin/resource/banners/{order} 成功删除轮播图")
    void deleteBanner() throws Exception {
        when(bannerService.delete(2)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/resource/banners/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(bannerService).delete(2);
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        AdminBannerController adminBannerController(BannerService bannerService) {
            return new AdminBannerController(bannerService);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }
}
