package com.rauio.smartdangjian.crosslayer.resource;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.resource.controller.user.UserBannerController;
import com.rauio.smartdangjian.server.resource.pojo.response.BannerResourceResponse;
import com.rauio.smartdangjian.server.resource.service.BannerService;

@SpringBootTest(classes = UserBannerControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("用户轮播图控制层集成测试")
class UserBannerControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private BannerService bannerService;

    @BeforeEach
    void setUp() {
        reset(bannerService);
    }

    @Test
    @DisplayName("GET /api/resource/banners 成功返回轮播图列表")
    void listBanners() throws Exception {
        BannerResourceResponse banner = new BannerResourceResponse(
                0, "1", "banner.png", "hash123", "key/banner.png", 0, 1, "https://example.com/banner.png");
        when(bannerService.getUserList()).thenReturn(List.of(banner));

        mockMvc.perform(get("/api/resource/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].order").value(0))
                .andExpect(jsonPath("$.data[0].originalName").value("banner.png"));

        verify(bannerService).getUserList();
    }

    @Test
    @DisplayName("GET /api/resource/banners/{order} 成功返回单个轮播图")
    void getBannerByOrder() throws Exception {
        BannerResourceResponse banner = new BannerResourceResponse(
                1, "2", "slide2.png", "hash456", "key/slide2.png", 0, 1, "https://example.com/slide2.png");
        when(bannerService.getUser(1)).thenReturn(banner);

        mockMvc.perform(get("/api/resource/banners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.order").value(1))
                .andExpect(jsonPath("$.data.resourceId").value("2"));

        verify(bannerService).getUser(1);
    }

    @Test
    @DisplayName("GET /api/resource/banners 空轮播图时不报错")
    void listBannersEmpty() throws Exception {
        when(bannerService.getUserList()).thenReturn(List.of());

        mockMvc.perform(get("/api/resource/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserBannerController userBannerController(BannerService bannerService) {
            return new UserBannerController(bannerService);
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
