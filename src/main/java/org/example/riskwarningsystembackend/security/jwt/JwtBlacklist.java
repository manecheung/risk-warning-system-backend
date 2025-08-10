package org.example.riskwarningsystembackend.security.jwt;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JWT黑名单实体类
 * 用于存储已登出但尚未过期的Token。
 */
@Data
@Entity
@Table(name = "jwt_blacklist")
@NoArgsConstructor
public class JwtBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    public JwtBlacklist(String token, Instant expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }
}