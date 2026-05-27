package com.rauio.smartdangjian.server.resource.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MultipartInitResponseTest {

    @Test
    @DisplayName("response 的 getter 返回正确值")
    void getters() {
        var uploadedPart = new MultipartUploadedPartResponse(1, "etag-value");
        var response = new MultipartInitResponse(
                false, "upload-id", "resource/abc.mp4", 5242880L, List.of(uploadedPart), null);

        assertThat(response.instantUpload()).isFalse();
        assertThat(response.uploadId()).isEqualTo("upload-id");
        assertThat(response.objectKey()).isEqualTo("resource/abc.mp4");
        assertThat(response.partSize()).isEqualTo(5242880L);
        assertThat(response.uploadedParts()).hasSize(1);
        assertThat(response.resourceId()).isNull();
    }

    @Test
    @DisplayName("instantUpload 为 true 时返回正确的响应")
    void instantUploadTrue() {
        var response = new MultipartInitResponse(true, null, null, null, null, "existing-resource-id");

        assertThat(response.instantUpload()).isTrue();
        assertThat(response.uploadId()).isNull();
        assertThat(response.resourceId()).isEqualTo("existing-resource-id");
    }
}
