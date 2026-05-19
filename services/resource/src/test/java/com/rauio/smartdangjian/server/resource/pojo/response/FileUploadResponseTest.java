package com.rauio.smartdangjian.server.resource.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FileUploadResponse 文件上传响应")
class FileUploadResponseTest {

    @Test
    @DisplayName("使用 builder 构造响应")
    void buildResponse() {
        FileUploadResponse resp = FileUploadResponse.builder()
                .resourceId("r-1")
                .uploadUrl("https://example.com/upload")
                .objectKey("image/test.png")
                .expiration(1714291200000L)
                .build();

        assertThat(resp.getResourceId()).isEqualTo("r-1");
        assertThat(resp.getUploadUrl()).isEqualTo("https://example.com/upload");
        assertThat(resp.getObjectKey()).isEqualTo("image/test.png");
        assertThat(resp.getExpiration()).isEqualTo(1714291200000L);
    }
}
