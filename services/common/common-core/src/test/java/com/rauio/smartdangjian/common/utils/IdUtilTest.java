package com.rauio.smartdangjian.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class IdUtilTest {

    @Nested
    @DisplayName("parse 方法")
    class ParseMethod {

        @Test
        @DisplayName("有效数字字符串返回正确 Long 值")
        void validStringReturnsLong() {
            assertThat(IdUtil.parse("123")).isEqualTo(123L);
        }

        @Test
        @DisplayName("零值字符串返回 0")
        void zeroStringReturnsZero() {
            assertThat(IdUtil.parse("0")).isZero();
        }

        @Test
        @DisplayName("负数返回负的 Long 值")
        void negativeStringReturnsNegativeLong() {
            assertThat(IdUtil.parse("-42")).isEqualTo(-42L);
        }

        @Test
        @DisplayName("最大 Long 值字符串解析成功")
        void maxLongStringParsesSuccessfully() {
            assertThat(IdUtil.parse(String.valueOf(Long.MAX_VALUE))).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("无效字符串抛出 BusinessException")
        void invalidStringThrowsBusinessException() {
            assertThatThrownBy(() -> IdUtil.parse("abc"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID格式错误");
        }

        @Test
        @DisplayName("空字符串抛出 BusinessException")
        void blankStringThrowsBusinessException() {
            assertThatThrownBy(() -> IdUtil.parse(""))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID格式错误");
        }

        @Test
        @DisplayName("null 字符串抛出 BusinessException")
        void nullStringThrowsBusinessException() {
            assertThatThrownBy(() -> IdUtil.parse(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID格式错误");
        }
    }

    @Nested
    @DisplayName("parseOrDefault 方法")
    class ParseOrDefaultMethod {

        @Test
        @DisplayName("有效字符串返回正确 Long 值")
        void validStringReturnsLong() {
            assertThat(IdUtil.parseOrDefault("456", 0L)).isEqualTo(456L);
        }

        @Test
        @DisplayName("无效字符串返回默认值")
        void invalidStringReturnsDefault() {
            assertThat(IdUtil.parseOrDefault("abc", 999L)).isEqualTo(999L);
        }

        @Test
        @DisplayName("空字符串返回默认值")
        void blankStringReturnsDefault() {
            assertThat(IdUtil.parseOrDefault("", -1L)).isEqualTo(-1L);
        }

        @Test
        @DisplayName("null 字符串返回默认值")
        void nullStringReturnsDefault() {
            assertThat(IdUtil.parseOrDefault(null, 0L)).isZero();
        }

        @Test
        @DisplayName("默认值为 null 时返回 null")
        void nullDefaultReturnsNull() {
            assertThat(IdUtil.parseOrDefault("abc", null)).isNull();
        }
    }

    @Nested
    @DisplayName("parseNullable 方法")
    class ParseNullableMethod {

        @Test
        @DisplayName("有效字符串返回正确 Long 值")
        void validStringReturnsLong() {
            assertThat(IdUtil.parseNullable("789")).isEqualTo(789L);
        }

        @Test
        @DisplayName("null 输入返回 null")
        void nullInputReturnsNull() {
            assertThat(IdUtil.parseNullable(null)).isNull();
        }

        @Test
        @DisplayName("空字符串返回 null")
        void blankStringReturnsNull() {
            assertThat(IdUtil.parseNullable("")).isNull();
        }

        @Test
        @DisplayName("空白字符串返回 null")
        void whitespaceStringReturnsNull() {
            assertThat(IdUtil.parseNullable("  ")).isNull();
        }

        @Test
        @DisplayName("无效字符串抛出 BusinessException")
        void invalidStringThrowsBusinessException() {
            assertThatThrownBy(() -> IdUtil.parseNullable("invalid"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID格式错误");
        }
    }
}
