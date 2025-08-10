package org.example.riskwarningsystembackend.module_system.dto;

import lombok.Data;
import org.example.riskwarningsystembackend.module_system.entity.Role;

/**
 * 角色数据传输对象
 */
@Data
public class RoleDto {
    private Long id;
    private String name;
    private String description;

    public static RoleDto fromEntity(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }
}
