package com.rauio.smartdangjian.server.auth.pojo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CaptchaTest {

    @Test
    @DisplayName("Builder 模式构建 Captcha 对象字段正确设置")
    void builderSetsFields() {
        Captcha captcha = Captcha.builder()
                .uuid("test-uuid-123")
                .code("ABCD")
                .base64("base64encodedstring")
                .build();

        assertThat(captcha.getUuid()).isEqualTo("test-uuid-123");
        assertThat(captcha.getCode()).isEqualTo("ABCD");
        assertThat(captcha.getBase64()).isEqualTo("base64encodedstring");
    }

    @Test
    @DisplayName("setter 设置字段后 getter 正确返回")
    void settersAndGettersWork() {
        Captcha captcha = Captcha.builder().build();

        captcha.setUuid("new-uuid");
        captcha.setCode("1234");
        captcha.setBase64("new-base64");

        assertThat(captcha.getUuid()).isEqualTo("new-uuid");
        assertThat(captcha.getCode()).isEqualTo("1234");
        assertThat(captcha.getBase64()).isEqualTo("new-base64");
    }

    @Test
    @DisplayName("code 字段可以为 null")
    void codeCanBeNull() {
        Captcha captcha = Captcha.builder().uuid("uuid").base64("base64").build();

        assertThat(captcha.getCode()).isNull();
    }
}
