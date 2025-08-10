package org.example.riskwarningsystembackend.module_system.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录请求的数据传输对象
 */
@Getter
@Setter
public class LoginRequest {
    private String username;
    private String password;
}
