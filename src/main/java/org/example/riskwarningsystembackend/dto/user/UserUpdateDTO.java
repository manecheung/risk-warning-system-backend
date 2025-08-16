package org.example.riskwarningsystembackend.dto.user;

import lombok.Data;
import java.util.Set;

/**
 * 用户更新数据传输对象
 * 用于封装用户更新操作所需的数据
 */
@Data
public class UserUpdateDTO {
    /**
     * 用户名称
     */
    private String name;

    /**
     * 用户密码
     */
    private String password; // 添加密码字段

    /**
     * 用户角色ID集合
     */
    private Set<Long> roleIds;

    /**
     * 用户所属组织ID
     */
    private Long organizationId;

    /**
     * 用户状态
     */
    private String status;
}
