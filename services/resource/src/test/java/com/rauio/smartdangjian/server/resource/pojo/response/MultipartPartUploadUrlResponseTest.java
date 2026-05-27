package com.rauio.smartdangjian.server.resource.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MultipartPartUploadUrlResponseTest {

    @Test
    @DisplayName("response 的 getter 返回正确值")
    void getters() {
        var response = new MultipartPartUploadUrlResponse("upload-id", 1, "https://example.com/upload");

        assertThat(response.uploadId()).isEqualTo("upload-id");
        assertThat(response.partNumber()).isEqualTo(1);
        assertThat(response.uploadUrl()).isEqualTo("https://example.com/upload");
    }
}
