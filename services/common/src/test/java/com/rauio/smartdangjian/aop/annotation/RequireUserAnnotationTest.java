package com.rauio.smartdangjian.aop.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequireUserAnnotationTest {

    @Test
    @DisplayName("RequireUser 注解可用于方法和类型")
    void annotationTargets() {
        RequireUser annotation = getAnnotation(AnnotatedClass.class, RequireUser.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.annotationType()).isEqualTo(RequireUser.class);
    }

    @RequireUser
    static class AnnotatedClass {
        @RequireUser
        public void method() {}
    }

    @RequireUser
    static class MethodLevel {
        public void method() {}
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass) {
        return clazz.getAnnotation(annotationClass);
    }
}
