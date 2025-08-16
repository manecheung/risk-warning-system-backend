package org.example.riskwarningsystembackend.dto.user;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据传输对象类
 * 用于在系统各层之间传输用户相关信息
 */
@Data
public class UserDTO {
    /**
     * 用户唯一标识符
     */
    private Long id;

    /**
     * 用户登录名
     */
    private String username;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户角色列表
     */
    private List<String> roles; // 修改: 单个角色变为角色列表

    /**
     * 用户所属组织
     */
    private String organization;

    /**
     * 用户状态
     */
    private String status;

    /**
     * 用户最后登录时间
     */
    private LocalDateTime lastLogin;
}
