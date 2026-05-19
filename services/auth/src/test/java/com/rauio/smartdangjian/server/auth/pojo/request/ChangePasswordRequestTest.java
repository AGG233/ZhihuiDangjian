package com.rauio.smartdangjian.server.auth.pojo.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChangePasswordRequestTest {

    @Test
    @DisplayName("无参构造创建对象后 setter 设置字段正确")
    void settersAndGettersWork() {
        ChangePasswordRequest request = new ChangePasswordRequest();

        request.setOldPassword("oldSecret123");
        request.setNewPassword("newSecret456");

        assertThat(request.getOldPassword()).isEqualTo("oldSecret123");
        assertThat(request.getNewPassword()).isEqualTo("newSecret456");
    }

    @Test
    @DisplayName("创建后字段默认为 null")
    void defaultFieldsAreNull() {
        ChangePasswordRequest request = new ChangePasswordRequest();

        assertThat(request.getOldPassword()).isNull();
        assertThat(request.getNewPassword()).isNull();
    }
}
