package com.rauio.ZhihuiDangjian.annotation.validation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rauio.ZhihuiDangjian.annotation.Serializer.SensitiveDataSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于数据脱敏
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveDataSerializer.class)
public @interface Sensitive {

    SensitiveType type();

    enum SensitiveType {
        PHONE,
        ID_CARD,
        BANK_CARD,
        PASSWORD,
        EMAIL
    }
}