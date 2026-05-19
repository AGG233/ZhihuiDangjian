package com.rauio.smartdangjian.server.resource.controller.user;

import com.rauio.smartdangjian.server.resource.pojo.response.BannerResourceResponse;
import com.rauio.smartdangjian.server.resource.service.BannerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBannerControllerTest {

    @Mock
    private BannerService bannerService;

    @InjectMocks
    private UserBannerController controller;

    @Test
    @DisplayName("list 委托 service 获取用户侧轮播图列表")
    void list() {
        when(bannerService.getUserList()).thenReturn(List.of(
                new BannerResourceResponse(0, "r-1", "b.png", "hash", "key", 0, 1, "url")
        ));

        var result = controller.list();

        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).resourceId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("get 委托 service 获取单个用户侧轮播图")
    void get() {
        when(bannerService.getUser(0)).thenReturn(
                new BannerResourceResponse(0, "r-1", "b.png", "hash", "key", 0, 1, "url")
        );

        var result = controller.get(0);

        assertThat(result.getData().resourceId()).isEqualTo("r-1");
    }
}
