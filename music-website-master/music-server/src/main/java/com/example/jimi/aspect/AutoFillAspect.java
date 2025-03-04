package com.example.jimi.aspect;

import com.example.jimi.annotation.AutoFill;
import com.example.jimi.enumeration.OperationType;
import com.example.jimi.model.request.ConsumerRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    // 定义切点：匹配所有 service 包下的方法，并且带有 @AutoFill 注解
    // 没有配置到mapper层 因为mybatis-plus的mapper接口是空的 实现起来反而麻烦 没有必要
    @Pointcut("execution(* com.example.jimi.service..*.*(..)) && @annotation(com.example.jimi.annotation.AutoFill)")
    public void autoFillPointCut() {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始自动填充时间字段");
        // 获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        // 假设第一个参数是 ConsumerRequest
        if (!(args[0] instanceof ConsumerRequest)) {
            return;
        }
        ConsumerRequest consumerRequest = (ConsumerRequest) args[0];
        Date now = new Date();
        System.out.println(now);
        // 根据操作类型填充时间
        if (operationType == OperationType.INSERT) {
            try {
                if (consumerRequest.getCreateTime() == null) {
                    consumerRequest.setCreateTime(now);
                }
                if (consumerRequest.getUpdateTime() == null) {
                    consumerRequest.setUpdateTime(now);
                }
            } catch (Exception e) {
                log.error("插入时自动填充时间失败: {}", e.getMessage());
            }
        } else if (operationType == OperationType.UPDATE) {
            try {
                consumerRequest.setUpdateTime(now);
            } catch (Exception e) {
                log.error("更新时自动填充时间失败: {}", e.getMessage());
            }
        }
    }
}