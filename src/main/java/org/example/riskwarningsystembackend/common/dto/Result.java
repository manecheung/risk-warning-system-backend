package org.example.riskwarningsystembackend.common.dto;

import lombok.Data;

/**
 * 统一API响应结果封装
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 私有构造函数，防止外部实例化
     */
    private Result() {}

    /**
     * 成功响应（无数据）
     * @return Result<Void>
     */
    public static Result<Void> success() {
        Result<Void> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功响应（有数据）
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（自定义消息和数据）
     * @param data 响应数据
     * @param message 自定义消息
     * @param <T>  数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败响应
     * @param code    状态码
     * @param message 错误信息
     * @return Result<Void>
     */
    public static Result<Void> error(Integer code, String message) {
        Result<Void> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}