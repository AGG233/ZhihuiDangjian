package com.rauio.ZhihuiDangjian.annotation.validation;

import com.rauio.ZhihuiDangjian.annotation.validation.Validator.IsPhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.FIELD,ElementType.ANNOTATION_TYPE,ElementType.CONSTRUCTOR,ElementType.PARAMETER,ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {IsPhoneValidator.class})
public @interface IsPhone {
    boolean required() default false;
    String message() default "手机格式错误";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
