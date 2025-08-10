package org.example.riskwarningsystembackend.module_system.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.common.dto.Result;
import org.example.riskwarningsystembackend.module_system.dto.LoginRequest;
import org.example.riskwarningsystembackend.module_system.dto.LoginResponse;
import org.example.riskwarningsystembackend.module_system.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 2.1 用户登录接口
     * @param loginRequest 登录请求体
     * @return 统一响应结果，包含Token
     */
    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(Result.success(loginResponse, "登录成功"));
    }

    /**
     * 用户登出接口
     * @param request HttpServletRequest 用于获取Authorization头
     * @return 统一响应结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        authService.logout(bearerToken);
        return ResponseEntity.ok(Result.success(null, "登出成功"));
    }
}