package org.example.riskwarningsystembackend.module_system.dto;

import lombok.Data;

/**
 * 创建用户时使用的数据传输对象
 */
@Data
public class UserCreateDto {
    private String username;
    private String name;
    private String password; // 注意：这里是明文密码
    private java.util.Set<Long> roleIds;
    private Long organizationId;
    private String status;
}