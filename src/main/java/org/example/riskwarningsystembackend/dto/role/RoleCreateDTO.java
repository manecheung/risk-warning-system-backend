package org.example.riskwarningsystembackend.dto.role;

import lombok.Data;

/**
 * RoleCreateDTO类用于创建角色的数据传输对象
 * 该类包含了创建角色时所需的基本信息
 */
@Data
public class RoleCreateDTO {
    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;
}

