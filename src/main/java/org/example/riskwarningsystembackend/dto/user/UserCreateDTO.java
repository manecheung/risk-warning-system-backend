package org.example.riskwarningsystembackend.dto.user;

import lombok.Data;
import java.util.Set;

/**
 * 用户创建数据传输对象
 * 用于封装用户创建时所需的数据信息
 */
@Data
public class UserCreateDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户角色ID集合
     */
    private Set<Long> roleIds; // 修改: 单个ID变为ID集合

    /**
     * 用户所属组织ID
     */
    private Long organizationId;

    /**
     * 用户状态
     */
    private String status;
}
