package org.example.riskwarningsystembackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT Token 提供者类，用于生成、解析和验证 JWT Token。
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-seconds}")
    private long jwtExpirationInSeconds;

    private Key key;

    /**
     * 初始化方法，在依赖注入完成后自动调用。
     * 根据配置的密钥字符串生成 HMAC-SHA 密钥对象。
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token。
     *
     * @param authentication Spring Security 的认证信息，包含用户主体信息
     * @return 生成的 JWT Token 字符串
     */
    public String generateToken(Authentication authentication) {
        // 将类型从 User 修改为 CustomUserDetails
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInSeconds * 1000);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // .getUsername() 是 UserDetails 接口的方法，所以这里不需要改动
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从 JWT Token 中提取用户名。
     *
     * @param token JWT Token 字符串
     * @return 提取到的用户名
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 从 HTTP 请求中解析出 JWT Token。
     *
     * @param request HTTP 请求对象
     * @return 解析出的 JWT Token 字符串，如果没有则返回 null
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 验证 JWT Token 是否有效。
     *
     * @param authToken 待验证的 JWT Token 字符串
     * @return 如果 Token 有效返回 true，否则返回 false
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}
