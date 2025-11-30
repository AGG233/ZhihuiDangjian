package com.rauio.ZhihuiDangjian.aop.annotation;

import com.rauio.ZhihuiDangjian.utils.Spec.UserType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionAccess {

    /**
    * 访问权限，默认为学生
    * */
    UserType value() default UserType.STUDENT;
}
