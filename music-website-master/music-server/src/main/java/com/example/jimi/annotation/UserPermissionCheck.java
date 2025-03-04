package com.example.jimi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserPermissionCheck {
    // 指定 DTO 中的用户 ID 字段名，默认 "id"
    String fieldName() default "id";
}