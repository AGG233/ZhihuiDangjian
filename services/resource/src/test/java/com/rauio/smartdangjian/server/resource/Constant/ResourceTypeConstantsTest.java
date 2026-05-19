package com.rauio.smartdangjian.server.resource.Constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResourceTypeConstants 资源类型常量")
class ResourceTypeConstantsTest {

    @Test
    @DisplayName("类型常量值正确")
    void constants() {
        assertThat(ResourceTypeConstants.IMAGE).isZero();
        assertThat(ResourceTypeConstants.VIDEO).isOne();
    }
}
