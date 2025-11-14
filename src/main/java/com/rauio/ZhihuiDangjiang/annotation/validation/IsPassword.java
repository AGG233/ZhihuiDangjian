package com.rauio.ZhihuiDangjiang.annotation.validation;


import com.rauio.ZhihuiDangjiang.annotation.validation.Validator.IsPasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Target({ElementType.METHOD,ElementType.FIELD,ElementType.ANNOTATION_TYPE,ElementType.CONSTRUCTOR,ElementType.PARAMETER,ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {IsPasswordValidator.class})
public @interface IsPassword{
    boolean required() default false;
    String message() default "密码格式错误";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

