package org.example.riskwarningsystembackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.login.LoginRequestDTO;
import org.example.riskwarningsystembackend.dto.login.LoginResponseDTO;
import org.example.riskwarningsystembackend.dto.user.UserInfoDTO;
import org.example.riskwarningsystembackend.entity.Permission;
import org.example.riskwarningsystembackend.security.CustomUserDetails;
import org.example.riskwarningsystembackend.security.JwtTokenProvider;
import org.example.riskwarningsystembackend.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * 认证控制器，用于处理用户登录和登出请求。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.expiration-seconds}")
    private long jwtExpirationInSeconds;

    /**
     * 构造函数，注入认证管理器、JWT提供者和Token黑名单服务。
     *
     * @param authenticationManager Spring Security 的认证管理器
     * @param tokenProvider         JWT Token 生成器
     * @param tokenBlacklistService Token 黑名单服务
     */
    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * 用户登录接口。
     * 根据用户名和密码进行身份验证，生成 JWT Token 并返回用户信息。
     *
     * @param loginRequestDTO 登录请求数据传输对象，包含用户名和密码
     * @return 登录响应结果，包含 JWT Token、过期时间和用户信息
     */
    @PostMapping("/login")
    public RestResult<LoginResponseDTO> authenticateUser(@RequestBody LoginRequestDTO loginRequestDTO) {

        // 使用用户名和密码进行身份验证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getUsername(),
                        loginRequestDTO.getPassword()
                )
        );

        // 将认证信息存入安全上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成 JWT Token
        String jwt = tokenProvider.generateToken(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 从用户详情中提取权限信息
        Set<String> permissions = userDetails.getUser().getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getKey)
                .collect(Collectors.toSet());

        // 构造用户信息 DTO
        UserInfoDTO userInfo = new UserInfoDTO(
                userDetails.getUser().getId(),
                userDetails.getUsername(),
                userDetails.getUser().getName(),
                permissions
        );

        // 构造登录响应 DTO
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO(jwt, jwtExpirationInSeconds, userInfo);

        return new RestResult<>(200, "登录成功", loginResponseDTO);
    }

    /**
     * 用户登出接口。
     * 解析请求中的 Token 并将其加入黑名单，实现登出功能。
     *
     * @param request HTTP 请求对象，用于获取 Token
     * @return 登出操作结果
     */
    @PostMapping("/logout")
    public RestResult<Void> logoutUser(HttpServletRequest request) {
        // 解析请求中的 Token
        String token = tokenProvider.resolveToken(request);
        if (StringUtils.hasText(token)) {
            // 将 Token 加入黑名单
            tokenBlacklistService.blacklistToken(token);
        }
        return RestResult.success();
    }
}
