package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.LoginRequest;
import org.example.riskwarningsystembackend.dto.LoginResponse;
import org.example.riskwarningsystembackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${jwt.expiration-seconds}")
    private long jwtExpirationInSeconds;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<RestResult<LoginResponse>> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        LoginResponse loginResponse = new LoginResponse(jwt, jwtExpirationInSeconds);

        return ResponseEntity.ok(new RestResult<>(200, "登录成功", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<RestResult<Void>> logoutUser() {
        // 对于无状态的JWT，服务器端无需做任何处理
        // 客户端应负责销毁本地Token
        // 此端点仅用于提供一个明确的登出API
        return ResponseEntity.ok(RestResult.success());
    }
}
