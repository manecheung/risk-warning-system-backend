package org.example.riskwarningsystembackend.module_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录响应的数据传输对象
 */
@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long expiresIn;
}
