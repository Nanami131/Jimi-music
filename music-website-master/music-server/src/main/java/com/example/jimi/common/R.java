package com.example.jimi.common;

import com.example.jimi.constant.RetTypeConstants;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 通用响应结果类，支持链式编程。
 * 通过静态方法快速生成不同类型的响应。
 */
@Data
@Accessors(chain = true) // 启用链式编程支持
public class R {

    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应类型
     */
    private String type;

    /**
     * 响应是否成功
     */
    private Boolean success;

    /**
     * 响应数据
     */
    private Object data;

    /**
     * 创建一个成功的响应结果。
     * @param message 成功消息
     * @return R 实例
     */
    public static R success(String message) {
        return new R()
                .setCode(200)
                .setMessage(message)
                .setSuccess(true)
                .setType(RetTypeConstants.RESULT_SUCCESS)
                .setData(null);
    }

    /**
     * 创建一个带数据的成功响应结果。
     * @param message 成功消息
     * @param data 返回的数据
     * @return R 实例
     */
    public static R success(String message, Object data) {
        return success(message).setData(data);
    }

    /**
     * 创建一个警告响应结果。
     * @param message 警告消息
     * @return R 实例
     */
    public static R warning(String message) {
        return error(message).setType(RetTypeConstants.RESULT_WARNING);
    }

    /**
     * 创建一个错误响应结果。
     * @param message 错误消息
     * @return R 实例
     */
    public static R error(String message) {
        return success(message)
                .setSuccess(false)
                .setType("RetTypeConstants.RESULT_ERROR");
    }

    /**
     * 创建一个严重错误响应结果。
     * @param message 错误消息
     * @return R 实例
     */
    public static R fatal(String message) {
        return error(message).setCode(500);
    }
}
