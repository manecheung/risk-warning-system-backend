package org.example.riskwarningsystembackend.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于管理JWT黑名单的服务.
 */
@Service
public class TokenBlacklistService {

    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    /**
     * 将token添加到黑名单.
     *
     * @param token 要拉黑的token
     */
    public void blacklistToken(String token) {
        blacklist.add(token);
    }

    /**
     * 检查token是否在黑名单中.
     *
     * @param token 要检查的token
     * @return 如果在黑名单中则返回true, 否则返回false
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
