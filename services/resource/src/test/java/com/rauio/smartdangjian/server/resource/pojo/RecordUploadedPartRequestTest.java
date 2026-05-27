package com.rauio.smartdangjian.server.resource.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecordUploadedPartRequestTest {

    @Test
    @DisplayName("request 的 getter 返回正确值")
    void getters() {
        var request = new RecordUploadedPartRequest("upload-id", 1, "etag-value");

        assertThat(request.uploadId()).isEqualTo("upload-id");
        assertThat(request.partNumber()).isEqualTo(1);
        assertThat(request.etag()).isEqualTo("etag-value");
    }
}
