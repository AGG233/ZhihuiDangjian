package com.rauio.smartdangjian.exception;

import static com.rauio.smartdangjian.constants.ErrorConstants.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BusinessExceptionTest {

    @Test
    @DisplayName("双参构造 (code, message) 设置 code 和 message")
    void constructorWithCodeAndMessage() {
        BusinessException ex = new BusinessException(1001, "自定义错误信息");

        assertThat(ex.getCode()).isEqualTo(1001);
        assertThat(ex.getMessage()).isEqualTo("自定义错误信息");
    }

    @Test
    @DisplayName("单参构造 (message) 设置 code 为 NOT_FOUND(1) 且 message 正确")
    void constructorWithMessageOnly() {
        BusinessException ex = new BusinessException("资源未找到");

        assertThat(ex.getCode()).isEqualTo(NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("资源未找到");
    }

    @Test
    @DisplayName("BusinessException 继承自 RuntimeException")
    void extendsRuntimeException() {
        BusinessException ex = new BusinessException("test");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getCode 返回设置的错误码")
    void getCodeReturnsSetValue() {
        BusinessException ex = new BusinessException(500, "服务器错误");

        assertThat(ex.getCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("getMessage 返回设置的消息")
    void getMessageReturnsSetValue() {
        BusinessException ex = new BusinessException(400, "请求参数错误");

        assertThat(ex.getMessage()).isEqualTo("请求参数错误");
    }
}
