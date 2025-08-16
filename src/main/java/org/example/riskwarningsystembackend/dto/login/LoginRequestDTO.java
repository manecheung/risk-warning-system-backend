package org.example.riskwarningsystembackend.dto.login;

import lombok.Data;

/**
 * 登录请求数据传输对象
 * 用于封装用户登录时提交的用户名和密码信息
 */
@Data
public class LoginRequestDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}

