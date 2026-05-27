package com.rauio.smartdangjian.server.resource.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;

@ExtendWith(MockitoExtension.class)
class ResourceMetaOwnerResolverTest {

    @Mock
    private ResourceMetaService resourceMetaService;

    @InjectMocks
    private ResourceMetaOwnerResolver resolver;

    @Test
    @DisplayName("supports 只支持 RESOURCE_META 类型")
    void supports() {
        assertThat(resolver.supports("RESOURCE_META")).isTrue();
        assertThat(resolver.supports("OTHER")).isFalse();
    }

    @Test
    @DisplayName("findResourceOwner 传入Long参数时返回上传人ID")
    void findResourceOwnerWithLongArgument() {
        when(resourceMetaService.get(1L))
                .thenReturn(ResourceMeta.builder().id(1L).uploaderId(1L).build());

        String owner = resolver.findResourceOwner(1L);

        assertThat(owner).isEqualTo("1");
    }

    @Test
    @DisplayName("findResourceOwner 返回上传人ID")
    void findResourceOwner() {
        when(resourceMetaService.get(1L))
                .thenReturn(
                        ResourceMeta.builder().id(1L).uploaderId(1L).build());

        String owner = resolver.findResourceOwner("1");

        assertThat(owner).isEqualTo("1");
    }
}
