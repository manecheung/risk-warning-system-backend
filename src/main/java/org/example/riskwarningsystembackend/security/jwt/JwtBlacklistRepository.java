package org.example.riskwarningsystembackend.security.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * JWT黑名单数据访问接口
 */
@Repository
public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {

    /**
     * 查找Token是否存在于黑名单
     * @param token JWT Token
     * @return Optional<JwtBlacklist>
     */
    Optional<JwtBlacklist> findByToken(String token);

    /**
     * 删除所有已过期的Token记录，用于定期清理
     * @param now 当前时间
     */
    void deleteByExpiryDateBefore(Instant now);
}