package org.example.riskwarningsystembackend.common;

import lombok.Getter;

/**
 * API 统一返回状态码枚举
 */
@Getter
public enum ResultCode {
    
    // --- 通用成功 ---
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),

    // --- 通用失败 ---
    FAILURE(500, "操作失败"),

    // --- 客户端错误 ---
    BAD_REQUEST(400, "错误的请求"),
    UNAUTHORIZED(401, "认证失败，请重新登录"),
    FORBIDDEN(403, "权限不足，禁止访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    // --- 服务端错误 ---
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    // --- 业务错误 ---
    LOGIN_ERROR(1001, "用户名或密码错误");


    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
