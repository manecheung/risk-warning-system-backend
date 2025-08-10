package org.example.riskwarningsystembackend.module_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色权限分配数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDto {
    private List<String> assignedKeys;
    private List<PermissionDto> permissionTree;
}
