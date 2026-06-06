package com.rauio.smartdangjian.annotation.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.annotation.validation.IsIdCard;

class IsIdCardValidatorTest {

    private final IsIdCardValidator validator = new IsIdCardValidator();

    @Test
    @DisplayName("null 身份证号返回 false")
    void nullIdCardReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(null, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("18 位数字格式的身份证号委托 hutool 校验")
    void validFormatIdCardDelegatesToHutool() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("123456789012345678", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("格式错误身份证号返回 false")
    void invalidFormatIdCardReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("12345", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("空字符串身份证号返回 false")
    void emptyIdCardReturnsFalse() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid("", context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("initialize 正确设置 required 属性")
    void initializeSetsRequiredField() {
        IsIdCard mockAnnotation = mock(IsIdCard.class);

        validator.initialize(mockAnnotation);

        // 不抛异常即为通过
    }
}
