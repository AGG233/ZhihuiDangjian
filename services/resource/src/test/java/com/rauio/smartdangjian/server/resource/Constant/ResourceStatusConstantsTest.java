package com.rauio.smartdangjian.server.resource.Constant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResourceStatusConstants 资源状态常量")
class ResourceStatusConstantsTest {

    @Test
    @DisplayName("状态常量值正确")
    void constants() {
        assertThat(ResourceStatusConstants.UPLOADING).isZero();
        assertThat(ResourceStatusConstants.PUBLIC).isOne();
        assertThat(ResourceStatusConstants.HIDDEN).isEqualTo(2);
    }
}
