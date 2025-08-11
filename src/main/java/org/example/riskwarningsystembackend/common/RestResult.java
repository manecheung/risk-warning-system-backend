package org.example.riskwarningsystembackend.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 统一API响应结果封装
 *
 * @param <T> 响应数据的类型
 */
@Getter
@Setter
public class RestResult<T> {

    private int code;
    private String message;
    private T data;

    public RestResult() {
    }

    public RestResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应的静态工厂方法
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return RestResult 实例
     */
    public static <T> RestResult<T> success(T data) {
        return new RestResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应的静态工厂方法（无数据）
     *
     * @return RestResult 实例
     */
    public static <T> RestResult<T> success() {
        return success(null);
    }
    
    /**
     * 成功响应的静态工厂方法（自定义消息）
     *
     * @param message 自定义成功消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return RestResult 实例
     */
    public static <T> RestResult<T> success(String message, T data) {
        return new RestResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应的静态工厂方法
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return RestResult 实例
     */
    public static <T> RestResult<T> failure(int code, String message) {
        return new RestResult<>(code, message, null);
    }

    /**
     * 失败响应的静态工厂方法（使用ResultCode枚举）
     *
     * @param resultCode 结果码枚举
     * @param <T>        数据类型
     * @return RestResult 实例
     */
    public static <T> RestResult<T> failure(ResultCode resultCode) {
        return new RestResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
}
