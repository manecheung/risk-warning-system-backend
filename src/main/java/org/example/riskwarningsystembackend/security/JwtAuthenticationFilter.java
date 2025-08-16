package org.example.riskwarningsystembackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.riskwarningsystembackend.service.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT身份验证过滤器，用于在每个请求中解析JWT并设置Spring Security的认证上下文。
 * <p>
 * 该过滤器继承自OncePerRequestFilter，确保每个请求只执行一次。
 * 它会从请求头中提取JWT，验证其有效性，并加载对应的用户信息进行认证。
 * 同时，它会检查令牌是否在黑名单中，以防止已注销的令牌继续使用。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * 构造函数，注入所需的依赖组件。
     *
     * @param tokenProvider         JWT令牌提供者，用于解析和验证JWT
     * @param userDetailsService    用户详情服务，用于根据用户名加载用户信息
     * @param tokenBlacklistService 令牌黑名单服务，用于检查令牌是否已被注销
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsServiceImpl userDetailsService, TokenBlacklistService tokenBlacklistService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * 执行过滤逻辑，在每个HTTP请求中解析JWT并设置认证信息。
     * <p>
     * 主要步骤包括：
     * 1. 从请求头中提取JWT；
     * 2. 检查令牌是否为空、是否在黑名单中以及是否有效；
     * 3. 若令牌有效，则加载用户信息并构建认证对象；
     * 4. 将认证对象设置到Spring Security上下文中；
     * 5. 继续执行后续过滤器链。
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链，用于继续处理请求
     * @throws ServletException 当Servlet处理出错时抛出
     * @throws IOException      当IO操作出错时抛出
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 提取JWT
            String jwt = getJwtFromRequest(request);

            // 验证JWT有效性并检查是否在黑名单中
            if (StringUtils.hasText(jwt) && !tokenBlacklistService.isTokenBlacklisted(jwt) && tokenProvider.validateToken(jwt)) {
                // 从JWT中获取用户名
                String username = tokenProvider.getUsernameFromJWT(jwt);

                // 加载用户详细信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // 构建认证对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置认证信息到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求头中提取JWT字符串。
     * <p>
     * 期望的请求头格式为："Authorization: Bearer <token>"。
     *
     * @param request HTTP请求对象
     * @return 提取到的JWT字符串；如果未找到或格式不正确，则返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
