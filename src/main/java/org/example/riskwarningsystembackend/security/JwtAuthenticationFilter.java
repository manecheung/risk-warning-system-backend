package org.example.riskwarningsystembackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.security.jwt.JwtBlacklistRepository;
import org.example.riskwarningsystembackend.security.jwt.JwtTokenProvider;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 在每个请求到达受保护资源之前，检查请求头中是否包含有效的JWT Token。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtBlacklistRepository jwtBlacklistRepository; // 注入黑名单Repository

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 检查JWT是否在黑名单中
                boolean isTokenBlacklisted = jwtBlacklistRepository.findByToken(jwt).isPresent();
                if (isTokenBlacklisted) {
                    // 如果在黑名单中，直接拒绝，不进行后续认证
                    SecurityContextHolder.clearContext(); // 确保清除上下文
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token is blacklisted and invalid.");
                    return;
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (tokenProvider.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception ex) {
            // 在这里可以记录认证失败的日志
            logger.error("无法设置用户认证", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求头中提取JWT Token
     * @param request HttpServletRequest
     * @return Token字符串或null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}