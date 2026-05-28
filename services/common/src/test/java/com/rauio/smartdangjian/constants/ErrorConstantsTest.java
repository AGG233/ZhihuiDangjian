package com.rauio.smartdangjian.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorConstantsTest {

    @Test
    @DisplayName("NOT_FOUND 常数值为 1")
    void notFound() {
        assertThat(ErrorConstants.NOT_FOUND).isEqualTo(1);
    }

    @Test
    @DisplayName("ARGS_ERROR 常数值为 2")
    void argsError() {
        assertThat(ErrorConstants.ARGS_ERROR).isEqualTo(2);
    }

    @Test
    @DisplayName("MAX_VALUE 常数值为 3")
    void maxValue() {
        assertThat(ErrorConstants.MAX_VALUE).isEqualTo(3);
    }

    @Test
    @DisplayName("MIN_VALUE 常数值为 4")
    void minValue() {
        assertThat(ErrorConstants.MIN_VALUE).isEqualTo(4);
    }

    @Test
    @DisplayName("RESOURCE_NOT_AUTHORIZED 常数值为 10")
    void resourceNotAuthorized() {
        assertThat(ErrorConstants.RESOURCE_NOT_AUTHORIZED).isEqualTo(10);
    }

    @Test
    @DisplayName("RESOURCE_NOT_EXISTS 常数值为 11")
    void resourceNotExists() {
        assertThat(ErrorConstants.RESOURCE_NOT_EXISTS).isEqualTo(11);
    }

    @Test
    @DisplayName("RESOURCE_NOT_AVAILABLE 常数值为 12")
    void resourceNotAvailable() {
        assertThat(ErrorConstants.RESOURCE_NOT_AVAILABLE).isEqualTo(12);
    }

    @Test
    @DisplayName("USER_NOT_EXISTS 常数值为 15")
    void userNotExists() {
        assertThat(ErrorConstants.USER_NOT_EXISTS).isEqualTo(15);
    }

    @Test
    @DisplayName("通用错误码范围在 1-99 之间")
    void errorCodeRange() {
        assertThat(ErrorConstants.NOT_FOUND).isBetween(1, 99);
        assertThat(ErrorConstants.ARGS_ERROR).isBetween(1, 99);
        assertThat(ErrorConstants.MAX_VALUE).isBetween(1, 99);
        assertThat(ErrorConstants.MIN_VALUE).isBetween(1, 99);
        assertThat(ErrorConstants.RESOURCE_NOT_AUTHORIZED).isBetween(1, 99);
        assertThat(ErrorConstants.RESOURCE_NOT_EXISTS).isBetween(1, 99);
        assertThat(ErrorConstants.RESOURCE_NOT_AVAILABLE).isBetween(1, 99);
        assertThat(ErrorConstants.USER_NOT_EXISTS).isBetween(1, 99);
    }
}
