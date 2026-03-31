package com.rauio.smartdangjian.aop.annotation;

import com.rauio.smartdangjian.aop.support.DataScopeAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScopeAccess {

    String resource();

    DataScopeAction action();

    String id() default "";

    String body() default "";

    String query() default "";
}
