package org.example.riskwarningsystembackend.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    FAILURE(500, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    NOT_FOUND(404, "资源未找到"),
    BAD_REQUEST(400, "错误的请求");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
