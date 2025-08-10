package org.example.riskwarningsystembackend.module_system.service;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_system.dto.LoginRequest;
import org.example.riskwarningsystembackend.module_system.dto.LoginResponse;
import org.example.riskwarningsystembackend.security.jwt.JwtBlacklist;
import org.example.riskwarningsystembackend.security.jwt.JwtBlacklistRepository;
import org.example.riskwarningsystembackend.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final JwtBlacklistRepository jwtBlacklistRepository; // 注入黑名单Repository

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 用户登录认证
     * @param loginRequest 登录请求
     * @return 包含Token的登录响应
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        // 使用AuthenticationManager进行用户认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 将认证信息设置到SecurityContext中
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT Token
        String token = tokenProvider.generateToken(authentication);

        return new LoginResponse(token, expiration / 1000); // 返回秒
    }

    /**
     * 用户登出
     * @param bearerToken "Bearer "前缀的token
     */
    @Transactional
    public void logout(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            // 从Token中获取过期时间
            Date expiryDate = tokenProvider.getExpirationDateFromToken(token);
            // 将Token存入黑名单
            JwtBlacklist blacklistedToken = new JwtBlacklist(token, expiryDate.toInstant());
            jwtBlacklistRepository.save(blacklistedToken);
            // 清除当前安全上下文
            SecurityContextHolder.clearContext();
        }
    }
}