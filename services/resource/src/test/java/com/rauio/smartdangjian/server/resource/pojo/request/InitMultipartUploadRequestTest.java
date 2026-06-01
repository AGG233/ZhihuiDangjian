package com.rauio.smartdangjian.server.resource.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InitMultipartUploadRequestTest {

    @Test
    @DisplayName("request 的 getter 返回正确值")
    void getters() {
        var request = new InitMultipartUploadRequest("hash123", "test.mp4", "mp4", "video/mp4", 104857600L, 5242880L);

        assertThat(request.fileHash()).isEqualTo("hash123");
        assertThat(request.fileName()).isEqualTo("test.mp4");
        assertThat(request.suffix()).isEqualTo("mp4");
        assertThat(request.contentType()).isEqualTo("video/mp4");
        assertThat(request.fileSize()).isEqualTo(104857600L);
        assertThat(request.partSize()).isEqualTo(5242880L);
    }

    @Test
    @DisplayName("request toString 包含关键字段")
    void toStringContainsFields() {
        var request = new InitMultipartUploadRequest("hash123", "test.mp4", "mp4", "video/mp4", 104857600L, 5242880L);
        assertThat(request.toString()).contains("hash123", "test.mp4");
    }
}
