package org.example.riskwarningsystembackend.module_system.dto;

import lombok.Data;

import java.util.Set;

/**
 * 更新用户时使用的数据传输对象
 */
@Data
public class UserUpdateDto {
    private String name;
    private String status;
    private Long organizationId;
    private Set<Long> roleIds;
    // 密码是可选的，只有当用户输入新密码时才更新
    private String password;
}