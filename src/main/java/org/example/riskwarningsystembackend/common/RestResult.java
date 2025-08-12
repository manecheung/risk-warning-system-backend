package org.example.riskwarningsystembackend.common;

import lombok.Data;

@Data
public class RestResult<T> {

    private int code;
    private String message;
    private T data;

    public RestResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> RestResult<T> success(T data) {
        return new RestResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> RestResult<T> success() {
        return success(null);
    }

    public static <T> RestResult<T> failure(ResultCode resultCode) {
        return new RestResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> RestResult<T> failure(ResultCode resultCode, String message) {
        return new RestResult<>(resultCode.getCode(), message, null);
    }
     public static <T> RestResult<T> failure(int code, String message) {
        return new RestResult<>(code, message, null);
    }
}
