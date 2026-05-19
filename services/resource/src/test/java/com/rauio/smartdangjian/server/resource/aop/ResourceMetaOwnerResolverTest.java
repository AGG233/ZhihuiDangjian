package com.rauio.smartdangjian.server.resource.aop;

import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
    @DisplayName("findResourceOwner 返回上传人ID")
    void findResourceOwner() {
        when(resourceMetaService.get("r-1")).thenReturn(
                ResourceMeta.builder().id("r-1").uploaderId("user-1").build()
        );

        String owner = resolver.findResourceOwner("r-1");

        assertThat(owner).isEqualTo("user-1");
    }
}
