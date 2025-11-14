package com.rauio.ZhihuiDangjiang.annotation.validation;


import com.rauio.ZhihuiDangjiang.annotation.validation.Validator.IsIdCardValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.FIELD,ElementType.ANNOTATION_TYPE,ElementType.CONSTRUCTOR,ElementType.PARAMETER,ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {IsIdCardValidator.class})
public @interface IsIdCard {
    boolean required() default false;
    String message() default "身份证格式错误";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
