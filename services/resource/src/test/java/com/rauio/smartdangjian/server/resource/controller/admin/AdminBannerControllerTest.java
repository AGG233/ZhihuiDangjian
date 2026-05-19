package com.rauio.smartdangjian.server.resource.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.BannerCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.BannerUpdateRequest;
import com.rauio.smartdangjian.server.resource.service.BannerService;

@ExtendWith(MockitoExtension.class)
class AdminBannerControllerTest {

    @Mock
    private BannerService bannerService;

    @InjectMocks
    private AdminBannerController controller;

    @Test
    @DisplayName("list 委托 service 获取轮播图列表")
    void list() {
        when(bannerService.getList())
                .thenReturn(List.of(ResourceMeta.builder().id("r-1").build()));

        var result = controller.list();

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("get 委托 service 获取单个轮播图")
    void get() {
        when(bannerService.get(1)).thenReturn(ResourceMeta.builder().id("r-1").build());

        var result = controller.get(1);

        assertThat(result.getData().getId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("create 使用 resourceId 创建轮播图")
    void createByResourceId() {
        BannerCreateRequest request = new BannerCreateRequest("r-1", null);
        when(bannerService.create("r-1"))
                .thenReturn(ResourceMeta.builder().id("r-1").build());

        var result = controller.create(request);

        assertThat(result.getData().getId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("create 使用 hash 创建轮播图")
    void createByHash() {
        BannerCreateRequest request = new BannerCreateRequest(null, "hash123");
        when(bannerService.createByHash("hash123"))
                .thenReturn(ResourceMeta.builder().id("r-1").build());

        var result = controller.create(request);

        assertThat(result.getData()).isNotNull();
    }

    @Test
    @DisplayName("update 使用 resourceId 更新轮播图")
    void updateByResourceId() {
        BannerUpdateRequest request = new BannerUpdateRequest("r-1", null);
        when(bannerService.update(1, "r-1"))
                .thenReturn(ResourceMeta.builder().id("r-1").build());

        var result = controller.update(1, request);

        assertThat(result.getData().getId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("delete 委托 service 删除轮播图")
    void delete() {
        when(bannerService.delete(1)).thenReturn(true);

        var result = controller.delete(1);

        assertThat(result.getData()).isTrue();
    }
}
