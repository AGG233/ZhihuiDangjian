package com.rauio.smartdangjian.server.resource.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;

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

    @Test
    @DisplayName("private 构造器覆盖")
    void privateConstructor() throws Exception {
        Constructor<ResourceConstant> constructor = ResourceConstant.class.getDeclaredConstructor();
        constructor.setAccessible(true); // NOSONAR
        constructor.newInstance();
    }
}
