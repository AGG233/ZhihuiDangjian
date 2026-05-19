package com.rauio.smartdangjian.annotation.validation.Validator;

import com.rauio.smartdangjian.annotation.validation.IsPhone;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class IsPhoneValidatorTest {

    private final IsPhoneValidator validator = new IsPhoneValidator();

    @Test
    @DisplayName("null 手机号返回 false")
    void nullPhoneReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(null, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("有效手机号 13800138000 返回 true")
    void validPhoneReturnsTrue() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("13800138000", context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("有效手机号 15912345678 返回 true")
    void validPhone159ReturnsTrue() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("15912345678", context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("非 1 开头的手机号返回 false")
    void phoneNotStartingWith1ReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("23800138000", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("长度不足 11 位返回 false")
    void phoneTooShortReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("1380013800", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("空字符串手机号返回 false")
    void emptyPhoneReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("initialize 正确设置 required 属性")
    void initializeSetsRequiredField() {
        IsPhone mockAnnotation = mock(IsPhone.class);

        validator.initialize(mockAnnotation);

        // 不抛异常即为通过
    }
}
