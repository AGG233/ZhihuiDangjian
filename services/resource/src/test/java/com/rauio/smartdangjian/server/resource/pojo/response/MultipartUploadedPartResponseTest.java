package com.rauio.smartdangjian.server.resource.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MultipartUploadedPartResponseTest {

    @Test
    @DisplayName("response 的 getter 返回正确值")
    void getters() {
        var response = new MultipartUploadedPartResponse(1, "etag-value");

        assertThat(response.partNumber()).isEqualTo(1);
        assertThat(response.etag()).isEqualTo("etag-value");
    }
}
