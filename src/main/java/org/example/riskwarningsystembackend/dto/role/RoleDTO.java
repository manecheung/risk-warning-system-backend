package org.example.riskwarningsystembackend.dto.role;

import lombok.Data;

/**
 * RoleDTO类用于表示角色数据传输对象
 * 该类包含了角色的基本信息，用于在系统各层之间传输角色数据
 */
@Data
public class RoleDTO {
    /**
     * 角色唯一标识符
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述信息
     */
    private String description;
}

