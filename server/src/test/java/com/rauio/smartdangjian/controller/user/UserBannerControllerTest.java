package com.rauio.smartdangjian.controller.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.rauio.smartdangjian.controller.factory.BannerTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.controller.user.UserBannerController;
import com.rauio.smartdangjian.server.resource.pojo.response.BannerResourceResponse;
import com.rauio.smartdangjian.server.resource.service.BannerService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserBannerControllerTest.TestConfig.class)
@DisplayName("用户轮播图接口测试")
class UserBannerControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserBannerController userBannerController(BannerService bannerService) {
            return new UserBannerController(bannerService);
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
            List<BannerResourceResponse> list = List.of(
                    BannerTestDataFactory.createBannerResourceResponse(0),
                    BannerTestDataFactory.createBannerResourceResponse(1));
            when(bannerService.getUserList()).thenReturn(list);

            mockMvc.perform(get("/api/resource/banners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("GET /{order} - 获取单个轮播图成功")
        void getBannerSuccess() throws Exception {
            when(bannerService.getUser(0)).thenReturn(BannerTestDataFactory.createBannerResourceResponse(0));

            mockMvc.perform(get("/api/resource/banners/0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.order").value(0));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400")
        void serviceThrowsBusinessException() throws Exception {
            when(bannerService.getUser(999)).thenThrow(new BusinessException(4000, "轮播图不存在"));

            mockMvc.perform(get("/api/resource/banners/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("轮播图不存在"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(bannerService.getUserList()).thenThrow(new RuntimeException("Redis连接失败"));

            mockMvc.perform(get("/api/resource/banners"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET / - 空列表返回空数组")
        void listBannersEmpty() throws Exception {
            when(bannerService.getUserList()).thenReturn(List.of());

            mockMvc.perform(get("/api/resource/banners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{order} - 负数 order 返回 BusinessException")
        void getBannerNegativeOrder() throws Exception {
            when(bannerService.getUser(-1)).thenThrow(new BusinessException(4000, "轮播图不存在"));

            mockMvc.perform(get("/api/resource/banners/-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("POST 请求查询接口返回 405")
        void postBannerReturns405() throws Exception {
            mockMvc.perform(post("/api/resource/banners")).andExpect(status().isMethodNotAllowed());
        }
    }
}
