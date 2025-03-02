package com.example.jimi.exception;

import com.example.jimi.common.R;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局异常处理器，统一处理系统中的异常。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理通用异常
     * @param e 异常对象
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleException(Exception e) {
        return R.error("系统内部错误: " + e.getMessage()).setCode(500);
    }

    /**
     * 处理自定义异常
     * @param e 自定义异常对象
     * @return 统一响应结果
     */
    @ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleCustomException(CustomException e) {
        return R.error(e.getMessage()+"全局异常处理器").setCode(e.getCode());
    }
}
