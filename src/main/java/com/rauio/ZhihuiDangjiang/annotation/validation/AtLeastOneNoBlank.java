package com.rauio.ZhihuiDangjiang.annotation.validation;


import com.rauio.ZhihuiDangjiang.annotation.validation.Validator.IsPasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {IsPasswordValidator.class})
public @interface AtLeastOneNoBlank {
    String message() default "指定的字段中至少有一个不能为空";
    String[] fields();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
