package org.example.riskwarningsystembackend.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.riskwarningsystembackend.dto.user.UserInfoDTO;

/**
 * 登录响应数据传输对象
 * 用于封装用户登录成功后的响应信息，包括认证令牌、过期时间和用户基本信息
 */
@Data
@AllArgsConstructor
public class LoginResponseDTO {
    /**
     * 认证令牌
     * 用于用户后续请求的身份验证
     */
    private String token;

    /**
     * 令牌过期时间
     * 表示令牌在多少秒后过期
     */
    private long expiresIn;

    /**
     * 用户信息
     * 包含登录用户的基本信息
     */
    private UserInfoDTO userInfo;
}

