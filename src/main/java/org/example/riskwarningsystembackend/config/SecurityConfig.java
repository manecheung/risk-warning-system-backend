package org.example.riskwarningsystembackend.config;

import org.example.riskwarningsystembackend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置类，用于配置Spring Security的安全策略。
 * 包括密码编码器、认证管理器、JWT过滤器链等安全组件的定义。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造函数，注入JWT认证过滤器。
     *
     * @param jwtAuthenticationFilter JWT认证过滤器实例
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置密码编码器，使用BCrypt算法对密码进行加密处理。
     *
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器，用于处理用户认证逻辑。
     *
     * @param authenticationConfiguration 认证配置信息
     * @return AuthenticationManager 认证管理器实例
     * @throws Exception 如果获取认证管理器失败
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 配置安全过滤器链，定义HTTP请求的访问控制策略。
     * 禁用CSRF保护，设置无状态会话管理，配置API访问权限，并添加JWT认证过滤器。
     *
     * @param http HttpSecurity对象，用于构建安全配置
     * @return SecurityFilterChain 安全过滤器链实例
     * @throws Exception 如果构建安全配置失败
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 禁用 CSRF 保护，适用于 RESTful API 场景
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 设置会话管理策略为无状态，不创建或使用 HttpSession
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权规则
                .authorizeHttpRequests(authz -> authz
                        // 1. 明确放行认证和错误处理相关的API
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        // 2. 明确保护所有其他的API请求
                        .requestMatchers("/api/**").authenticated()
                        // 3. 其他所有请求（非/api/开头的）全部放行。
                        //    这包括前端静态资源和所有前端路由。
                        .anyRequest().permitAll()
                );

        // 在用户名密码认证过滤器之前添加 JWT 认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
