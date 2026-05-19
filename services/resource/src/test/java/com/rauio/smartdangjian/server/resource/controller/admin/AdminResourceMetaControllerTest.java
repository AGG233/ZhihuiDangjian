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
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaUpdateRequest;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;

@ExtendWith(MockitoExtension.class)
class AdminResourceMetaControllerTest {

    @Mock
    private ResourceMetaService resourceMetaService;

    @InjectMocks
    private AdminResourceMetaController controller;

    @Test
    @DisplayName("create 委托 service 创建资源元数据")
    void create() {
        ResourceMetaCreateRequest request = new ResourceMetaCreateRequest();
        request.setUploaderId("user-1");
        request.setOriginalName("test.png");
        request.setHash("hash123");
        request.setObjectKey("image/test.png");
        request.setResourceType(0);

        when(resourceMetaService.create(request))
                .thenReturn(ResourceMeta.builder().id("r-1").build());

        var result = controller.create(request);

        assertThat(result.getData().getId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("get 委托 service 获取资源元数据")
    void get() {
        when(resourceMetaService.get("r-1"))
                .thenReturn(ResourceMeta.builder().id("r-1").build());

        var result = controller.get("r-1");

        assertThat(result.getData().getId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("list 委托 service 查询资源列表")
    void list() {
        when(resourceMetaService.list("user-1", null, null, null, null))
                .thenReturn(List.of(ResourceMeta.builder().id("r-1").build()));

        var result = controller.list("user-1", null, null, null, null);

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("update 委托 service 更新资源")
    void update() {
        ResourceMetaUpdateRequest request = new ResourceMetaUpdateRequest();
        when(resourceMetaService.update("r-1", request)).thenReturn(true);

        var result = controller.update("r-1", request);

        assertThat(result.getData()).isTrue();
    }

    @Test
    @DisplayName("deleteById 委托 service 删除资源")
    void deleteById() {
        when(resourceMetaService.delete("r-1")).thenReturn(true);

        var result = controller.deleteById("r-1");

        assertThat(result.getData()).isTrue();
    }

    @Test
    @DisplayName("delete 按哈希删除")
    void deleteByHash() {
        when(resourceMetaService.deleteByHash("hash123")).thenReturn(true);

        var result = controller.delete("hash123");

        assertThat(result.getData()).isTrue();
    }

    @Test
    @DisplayName("delete 批量按哈希删除")
    void deleteByHashes() {
        when(resourceMetaService.deleteByHashes(List.of("h1", "h2"))).thenReturn(true);

        var result = controller.delete(new String[] {"h1", "h2"});

        assertThat(result.getData()).isTrue();
    }
}
