package com.rauio.smartdangjian.server.resource.pojo.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileInfoResponse 文件信息响应")
class FileInfoResponseTest {

    @Test
    @DisplayName("使用 builder 构造响应")
    void buildResponse() {
        FileInfoResponse resp = FileInfoResponse.builder()
                .resourceId("r-1")
                .originalName("test.png")
                .hash("hash123")
                .objectKey("image/test.png")
                .resourceType(0)
                .status(1)
                .downloadUrl("https://example.com/download")
                .size(1024L)
                .build();

        assertThat(resp.getResourceId()).isEqualTo("r-1");
        assertThat(resp.getOriginalName()).isEqualTo("test.png");
        assertThat(resp.getHash()).isEqualTo("hash123");
        assertThat(resp.getObjectKey()).isEqualTo("image/test.png");
        assertThat(resp.getResourceType()).isZero();
        assertThat(resp.getStatus()).isOne();
        assertThat(resp.getDownloadUrl()).isEqualTo("https://example.com/download");
        assertThat(resp.getSize()).isEqualTo(1024L);
    }
}
