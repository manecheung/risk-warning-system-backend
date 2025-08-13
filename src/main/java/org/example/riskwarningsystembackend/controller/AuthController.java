package org.example.riskwarningsystembackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.LoginRequest;
import org.example.riskwarningsystembackend.dto.LoginResponse;
import org.example.riskwarningsystembackend.dto.UserInfoDTO;
import org.example.riskwarningsystembackend.entity.Permission;
import org.example.riskwarningsystembackend.security.CustomUserDetails;
import org.example.riskwarningsystembackend.security.JwtTokenProvider;
import org.example.riskwarningsystembackend.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.expiration-seconds}")
    private long jwtExpirationInSeconds;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
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
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 从 UserDetails 中提取信息创建 UserInfoDTO
        Set<String> permissions = userDetails.getUser().getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getKey)
                .collect(Collectors.toSet());

        UserInfoDTO userInfo = new UserInfoDTO(
                userDetails.getUser().getId(),
                userDetails.getUsername(),
                userDetails.getUser().getName(),
                permissions
        );

        LoginResponse loginResponse = new LoginResponse(jwt, jwtExpirationInSeconds, userInfo);

        return ResponseEntity.ok(new RestResult<>(200, "登录成功", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<RestResult<Void>> logoutUser(HttpServletRequest request) {
        String token = tokenProvider.resolveToken(request);
        if (StringUtils.hasText(token)) {
            tokenBlacklistService.blacklistToken(token);
        }
        return ResponseEntity.ok(RestResult.success());
    }
}
