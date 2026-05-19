package com.rauio.smartdangjian.server.resource.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResourceErrorConstants 资源模块错误码常量")
class ResourceErrorConstantsTest {

    @Test
    @DisplayName("资源元数据错误码在 5000-5010 范围内")
    void resourceErrorCodesInRange() {
        assertThat(ResourceErrorConstants.RESOURCE_CREATE_FAILED).isEqualTo(5001);
        assertThat(ResourceErrorConstants.RESOURCE_NOT_FOUND).isEqualTo(5002);
        assertThat(ResourceErrorConstants.RESOURCE_UPDATE_FAILED).isEqualTo(5003);
        assertThat(ResourceErrorConstants.RESOURCE_DELETE_FAILED).isEqualTo(5004);
        assertThat(ResourceErrorConstants.RESOURCE_HASH_EXISTS).isEqualTo(5005);
        assertThat(ResourceErrorConstants.RESOURCE_OBJECT_KEY_EXISTS).isEqualTo(5006);

        assertThat(ResourceErrorConstants.RESOURCE_CREATE_FAILED).isBetween(5000, 5999);
    }

    @Test
    @DisplayName("轮播图错误码在 5011-5020 范围内")
    void bannerErrorCodesInRange() {
        assertThat(ResourceErrorConstants.BANNER_NOT_FOUND).isEqualTo(5011);
        assertThat(ResourceErrorConstants.BANNER_RESOURCE_EMPTY).isEqualTo(5012);
        assertThat(ResourceErrorConstants.BANNER_MAX_SIZE).isEqualTo(5013);
        assertThat(ResourceErrorConstants.BANNER_ALREADY_EXISTS).isEqualTo(5014);
        assertThat(ResourceErrorConstants.BANNER_CREATE_FAILED).isEqualTo(5015);
        assertThat(ResourceErrorConstants.BANNER_ID_AND_HASH_EMPTY).isEqualTo(5016);
    }
}
