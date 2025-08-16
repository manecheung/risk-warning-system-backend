package org.example.riskwarningsystembackend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，用于统一处理系统中的异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理运行时异常的全局异常处理器
     *
     * @param ex 运行时异常对象
     * @return 包含错误信息的响应实体，状态码为400
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<RestResult<Object>> handleRuntimeException(RuntimeException ex) {
        // 记录运行时异常日志
        log.error("发生运行时异常: ", ex);

        // 构造失败响应结果
        RestResult<Object> result = RestResult.failure(ResultCode.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
}

