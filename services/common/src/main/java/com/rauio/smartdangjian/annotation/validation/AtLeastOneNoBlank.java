package com.rauio.smartdangjian.annotation.validation;

import com.rauio.smartdangjian.annotation.validation.Validator.AtLeastOneNoBlankValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = AtLeastOneNoBlankValidator.class)
public @interface AtLeastOneNoBlank {
    String message() default "指定的字段中至少有一个不能为空";

    String[] fields();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
