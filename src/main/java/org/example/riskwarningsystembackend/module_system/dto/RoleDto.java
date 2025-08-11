package org.example.riskwarningsystembackend.module_system.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.example.riskwarningsystembackend.module_system.entity.Permission;
import org.example.riskwarningsystembackend.module_system.entity.Role;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色数据传输对象
 */
@Data
public class RoleDto {
    private Long id;

    @NotEmpty(message = "角色名称不能为空")
    private String name;

    @NotEmpty(message = "描述不能为空")
    private String description;
    private Set<String> permissions;

    public static RoleDto fromEntity(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        if (role.getPermissions() != null) {
            dto.setPermissions(role.getPermissions().stream()
                    .map(Permission::getPermissionKey)
                    .collect(Collectors.toSet()));
        } else {
            dto.setPermissions(Collections.emptySet());
        }
        return dto;
    }
}
