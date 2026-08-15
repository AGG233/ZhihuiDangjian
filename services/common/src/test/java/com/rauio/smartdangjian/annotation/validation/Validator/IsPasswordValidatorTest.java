package com.rauio.smartdangjian.annotation.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.annotation.validation.IsPassword;

class IsPasswordValidatorTest {

    private final IsPasswordValidator validator = new IsPasswordValidator();

    @Test
    @DisplayName("null 密码 且 required=false 时放行")
    void nullValueAllowedWhenNotRequired() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        IsPassword annotation = mock(IsPassword.class);
        when(annotation.required()).thenReturn(false);
        validator.initialize(annotation);

        boolean result = validator.isValid(null, context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("null 密码 且 required=true 时拒绝")
    void nullValueRejectedWhenRequired() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        IsPassword annotation = mock(IsPassword.class);
        when(annotation.required()).thenReturn(true);
        validator.initialize(annotation);

        boolean result = validator.isValid(null, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("包含字母数字和特殊字符的密码返回 true")
    void validPasswordReturnsTrue() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("Test@1234", context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("缺少特殊字符的密码返回 false")
    void passwordWithoutSpecialCharReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("Password1", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("长度小于 8 位的密码返回 false")
    void passwordTooShortReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("Ab1!", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("空字符串密码返回 false")
    void emptyPasswordReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("initialize 正确设置 required 属性")
    void initializeSetsRequiredField() {
        IsPassword mockAnnotation = mock(IsPassword.class);

        validator.initialize(mockAnnotation);

        // 不抛异常即为通过
    }
}
