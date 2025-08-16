package org.example.riskwarningsystembackend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 用户信息数据传输对象
 * 用于在系统各层之间传输用户基本信息和权限数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    /**
     * 用户唯一标识符
     */
    private Long id;

    /**
     * 用户登录名
     */
    private String username;

    /**
     * 用户显示名称
     */
    private String name;

    /**
     * 用户权限集合
     */
    private Set<String> permissions;
}

