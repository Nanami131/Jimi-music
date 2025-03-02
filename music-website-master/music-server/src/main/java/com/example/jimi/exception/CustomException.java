package com.example.jimi.exception;

/**
 * 自定义异常类，用于处理业务异常，支持返回更多信息。
 */
public class CustomException extends RuntimeException {

    private final int code; // 错误代码
    private final String details; // 错误详细信息

    /**
     * 构造方法
     * @param message 错误消息
     * @param code 错误代码
     * @param details 错误详细信息
     */
    public CustomException(String message, int code, String details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    /**
     * 构造方法（无详细信息）
     * @param message 错误消息
     * @param code 错误代码
     */
    public CustomException(String message, int code) {
        this(message, code, null);
    }

    /**
     * 获取错误代码
     * @return 错误代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误详细信息
     * @return 错误详细信息
     */
    public String getDetails() {
        return details;
    }
}
