package org.example.riskwarningsystembackend.dto.role;

import lombok.Data;

/**
 * RoleUpdateDTO类用于封装角色更新的数据传输对象
 * 该类包含了角色的基本信息，用于前后端数据传输
 */
@Data
public class RoleUpdateDTO {
    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述信息
     */
    private String description;
}

