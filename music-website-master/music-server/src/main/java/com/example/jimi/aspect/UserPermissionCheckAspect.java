package com.example.jimi.aspect;

import com.example.jimi.annotation.UserPermissionCheck;
import com.example.jimi.handler.ConsumerDTOHandler;
import com.example.jimi.model.domain.ConsumerDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class UserPermissionCheckAspect {

    // 定义切点：service/impl下的方法
    @Pointcut("execution(* com.example.jimi.service..*.*(..)) && @annotation(com.example.jimi.annotation.UserPermissionCheck)")
    public void permissionCheckPointCut() {}

    @Before("permissionCheckPointCut()")
    public void checkPermission(JoinPoint joinPoint) throws Exception {
        log.info("开始用户权限校验");

        ConsumerDTO currentUser = ConsumerDTOHandler.getConsumerInfo();
        if (currentUser == null || currentUser.getId() == null) {
            log.error("ThreadLocal 用户信息为空，无法校验权限");
            throw new RuntimeException("用户未登录或信息缺失");
        }
        Integer currentUserId = currentUser.getId();
        log.info("当前用户 ID: {}", currentUserId);

        // 获取方法签名和注解 UserPermissionCheck
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        UserPermissionCheck permissionCheck = signature.getMethod().getAnnotation(UserPermissionCheck.class);
        String fieldName = permissionCheck.fieldName();

        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            log.error("方法参数为空，无法校验权限");
            throw new IllegalArgumentException("请求参数缺失");
        }

        // 约定第一个参数是 DTO实体 提取其 ID
        Object dto = args[0];
        if (dto == null) {
            log.error("DTO 参数为空，无法校验权限");
            throw new IllegalArgumentException("DTO 参数为空");
        }

        // 反射获取 DTO 中的 ID 值
        Integer requestUserId;
        try {
            Field field = dto.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object idValue = field.get(dto);
            if (idValue == null) {
                log.error("DTO 中的 {} 字段为空", fieldName);
                throw new IllegalArgumentException("用户 ID 字段为空");
            }
            requestUserId = (Integer) idValue;
        } catch (NoSuchFieldException e) {
            log.error("DTO 中未找到字段: {}", fieldName);
            throw new IllegalArgumentException("无效的用户 ID 字段名: " + fieldName);
        }

        // 校验权限
        if (!Objects.equals(currentUserId, requestUserId)) {
            log.warn("权限校验失败，当前用户 ID: {}, 请求用户 ID: {}", currentUserId, requestUserId);
            throw new RuntimeException("无权限修改他人信息");
        }
        log.info("权限校验通过，用户 ID: {}", currentUserId);
    }
}