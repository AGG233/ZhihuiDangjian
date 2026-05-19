package com.rauio.smartdangjian.server.resource.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResourceMeta 资源元数据实体")
class ResourceMetaTest {

    @Test
    @DisplayName("使用 builder 构造实体")
    void buildEntity() {
        ResourceMeta meta = ResourceMeta.builder()
                .id("r-1")
                .uploaderId("user-1")
                .originalName("test.png")
                .hash("abc123")
                .objectKey("image/abc123.png")
                .resourceType(0)
                .status(1)
                .build();

        assertThat(meta.getId()).isEqualTo("r-1");
        assertThat(meta.getUploaderId()).isEqualTo("user-1");
        assertThat(meta.getOriginalName()).isEqualTo("test.png");
        assertThat(meta.getHash()).isEqualTo("abc123");
        assertThat(meta.getObjectKey()).isEqualTo("image/abc123.png");
        assertThat(meta.getResourceType()).isZero();
        assertThat(meta.getStatus()).isOne();
    }
}
