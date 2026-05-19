package com.rauio.smartdangjian.annotation.validation.Validator;

import com.rauio.smartdangjian.annotation.validation.AtLeastOneNoBlank;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AtLeastOneNoBlankValidatorTest {

    static class TestObject {
        private String fieldA;
        private String fieldB;
        private String fieldC;

        public TestObject(String a, String b, String c) {
            this.fieldA = a;
            this.fieldB = b;
            this.fieldC = c;
        }

        public String getFieldA() { return fieldA; }
        public String getFieldB() { return fieldB; }
        public String getFieldC() { return fieldC; }
    }

    @Test
    @DisplayName("initialize 从注解读取 fields 属性")
    void initializeReadsFieldsFromAnnotation() {
        AtLeastOneNoBlankValidator validator = new AtLeastOneNoBlankValidator();
        AtLeastOneNoBlank mockAnnotation = mock(AtLeastOneNoBlank.class);
        when(mockAnnotation.fields()).thenReturn(new String[]{"fieldA", "fieldB"});

        validator.initialize(mockAnnotation);

        // 不抛异常即为通过
    }

    @Test
    @DisplayName("对象为 null 时返回 true")
    void nullObjectReturnsTrue() {
        AtLeastOneNoBlankValidator validator = new AtLeastOneNoBlankValidator();
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(null, context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("至少有一个字段不为空时返回 true")
    void atLeastOneNonBlankReturnsTrue() {
        AtLeastOneNoBlankValidator validator = new AtLeastOneNoBlankValidator();
        AtLeastOneNoBlank mockAnnotation = mock(AtLeastOneNoBlank.class);
        when(mockAnnotation.fields()).thenReturn(new String[]{"fieldA", "fieldB"});
        validator.initialize(mockAnnotation);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        TestObject obj = new TestObject("", "hello", "");

        boolean result = validator.isValid(obj, context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("所有指定字段都为空时返回 false")
    void allFieldsBlankReturnsFalse() {
        AtLeastOneNoBlankValidator validator = new AtLeastOneNoBlankValidator();
        AtLeastOneNoBlank mockAnnotation = mock(AtLeastOneNoBlank.class);
        when(mockAnnotation.fields()).thenReturn(new String[]{"fieldA", "fieldB"});
        validator.initialize(mockAnnotation);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        TestObject obj = new TestObject("", "", "");

        boolean result = validator.isValid(obj, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("指定字段不存在时抛出 RuntimeException")
    void nonExistentFieldThrowsRuntimeException() {
        AtLeastOneNoBlankValidator validator = new AtLeastOneNoBlankValidator();
        AtLeastOneNoBlank mockAnnotation = mock(AtLeastOneNoBlank.class);
        when(mockAnnotation.fields()).thenReturn(new String[]{"nonExistent"});
        validator.initialize(mockAnnotation);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        TestObject obj = new TestObject("a", "b", "c");

        assertThatThrownBy(() -> validator.isValid(obj, context))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("三个字段中字段C不为空时返回 true")
    void thirdFieldNonBlankReturnsTrue() {
        AtLeastOneNoBlankValidator validator = new AtLeastOneNoBlankValidator();
        AtLeastOneNoBlank mockAnnotation = mock(AtLeastOneNoBlank.class);
        when(mockAnnotation.fields()).thenReturn(new String[]{"fieldA", "fieldB", "fieldC"});
        validator.initialize(mockAnnotation);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        TestObject obj = new TestObject("", "", "c-value");

        boolean result = validator.isValid(obj, context);

        assertThat(result).isTrue();
    }
}
