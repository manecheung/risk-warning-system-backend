package org.example.riskwarningsystembackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 自定义异常类，用于表示资源未找到的异常情况
 * 当系统中请求的资源不存在时抛出此异常
 * 该异常会自动返回HTTP 404状态码
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 构造一个ResourceNotFoundException异常实例
     *
     * @param message 异常信息描述，说明导致异常的具体原因
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

