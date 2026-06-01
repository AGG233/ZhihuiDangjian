package com.rauio.smartdangjian.server.resource.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MultipartProgressResponseTest {

    @Test
    @DisplayName("response 的 getter 返回正确值")
    void getters() {
        var part = new MultipartUploadedPartResponse(1, "etag-value");
        var response = new MultipartProgressResponse(
                "upload-id", "resource/abc.mp4", "UPLOADING", List.of(part));

        assertThat(response.uploadId()).isEqualTo("upload-id");
        assertThat(response.objectKey()).isEqualTo("resource/abc.mp4");
        assertThat(response.status()).isEqualTo("UPLOADING");
        assertThat(response.uploadedParts()).hasSize(1);
        assertThat(response.uploadedParts().getFirst().partNumber()).isEqualTo(1);
    }
}
