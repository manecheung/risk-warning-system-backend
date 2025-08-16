package org.example.riskwarningsystembackend.common;

import lombok.Getter;

/**
 * 统一API响应结果封装类
 * 用于封装RESTful API的返回结果，包含状态码、消息和数据
 *
 * @param <T> 数据类型泛型参数
 */
@Getter
public class RestResult<T> {

    /**
     * 响应状态码
     */
    private final int code;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 响应数据
     */
    private final T data;

    /**
     * 构造函数，初始化RestResult对象
     *
     * @param code    响应状态码
     * @param message 响应消息
     * @param data    响应数据
     */
    public RestResult(int code, String message, T data) {
        this.code = code;
        this.message = (message != null) ? message : "";
        this.data = data;
    }

    /**
     * 创建成功的响应结果
     *
     * @param data 成功时返回的数据
     * @param <T>  数据类型泛型参数
     * @return 成功的RestResult对象
     */
    public static <T> RestResult<T> success(T data) {
        return new RestResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 创建成功的响应结果（无数据）
     *
     * @param <T> 数据类型泛型参数
     * @return 成功的RestResult对象
     */
    public static <T> RestResult<T> success() {
        return success(null);
    }

    /**
     * 创建失败的响应结果
     *
     * @param resultCode 结果码枚举对象
     * @param <T>        数据类型泛型参数
     * @return 失败的RestResult对象
     */
    public static <T> RestResult<T> failure(ResultCode resultCode) {
        return new RestResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 创建失败的响应结果，可自定义消息
     *
     * @param resultCode 结果码枚举对象
     * @param message    自定义错误消息
     * @param <T>        数据类型泛型参数
     * @return 失败的RestResult对象
     */
    public static <T> RestResult<T> failure(ResultCode resultCode, String message) {
        return new RestResult<>(resultCode.getCode(), message, null);
    }

    /**
     * 创建失败的响应结果，可自定义状态码和消息
     *
     * @param code    自定义状态码
     * @param message 自定义错误消息
     * @param <T>     数据类型泛型参数
     * @return 失败的RestResult对象
     */
    public static <T> RestResult<T> failure(int code, String message) {
        return new RestResult<>(code, message, null);
    }
}
