package com.rauio.smartdangjian.server.resource.Constant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResourceConstant 资源常量")
class ResourceConstantTest {

    @Test
    @DisplayName("常量值正确")
    void constants() {
        assertThat(ResourceConstant.BANNER_PREFIX).isEqualTo("BANNER:");
        assertThat(ResourceConstant.BANNER_MAX_SIZE).isEqualTo(100);
        assertThat(ResourceConstant.COS_PLATFORM).isEqualTo("tencent-cos");
        assertThat(ResourceConstant.COS_KEY_EXPIRATION).isEqualTo(10 * 60 * 1000);
    }
}
