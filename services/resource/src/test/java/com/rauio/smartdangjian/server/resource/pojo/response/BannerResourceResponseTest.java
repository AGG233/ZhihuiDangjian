package com.rauio.smartdangjian.server.resource.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BannerResourceResponse 用户侧轮播图响应")
class BannerResourceResponseTest {

    @Test
    @DisplayName("使用 record 构造响应")
    void buildResponse() {
        BannerResourceResponse resp = new BannerResourceResponse(
                1, "r-1", "banner.png", "hash123", "image/banner.png", 0, 1, "https://example.com/download");

        assertThat(resp.order()).isEqualTo(1);
        assertThat(resp.resourceId()).isEqualTo("r-1");
        assertThat(resp.originalName()).isEqualTo("banner.png");
        assertThat(resp.hash()).isEqualTo("hash123");
        assertThat(resp.resourceType()).isZero();
        assertThat(resp.status()).isOne();
        assertThat(resp.downloadUrl()).isEqualTo("https://example.com/download");
    }
}
