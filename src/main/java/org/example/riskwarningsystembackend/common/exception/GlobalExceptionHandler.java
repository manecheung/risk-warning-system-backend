package org.example.riskwarningsystembackend.common.exception;

import org.example.riskwarningsystembackend.common.dto.Result;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 捕获Controller层抛出的异常，并返回统一的错误响应格式。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理数据完整性违规异常 (例如，唯一约束冲突)
     * @param e DataIntegrityViolationException
     * @return ResponseEntity<Result<Void>>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        // 在生产环境中，这里应该记录详细的错误日志
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 Conflict
                .body(Result.error(HttpStatus.CONFLICT.value(), "数据冲突，可能存在重复的唯一键值。"));
    }

    /**
     * 处理认证失败异常 (例如，用户名或密码错误)
     * @param e BadCredentialsException
     * @return ResponseEntity<Result<Void>>
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Result<Void>> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误"));
    }

    /**
     * 处理权限不足异常
     * @param e AccessDeniedException
     * @return ResponseEntity<Result<Void>>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Result.error(HttpStatus.FORBIDDEN.value(), "权限不足，禁止访问"));
    }

    /**
     * 处理所有未被捕获的运行时异常
     * @param e Exception
     * @return ResponseEntity<Result<Void>>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleAllExceptions(Exception e) {
        // 在生产环境中，这里应该记录详细的错误日志 e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误: " + e.getMessage()));
    }
}
