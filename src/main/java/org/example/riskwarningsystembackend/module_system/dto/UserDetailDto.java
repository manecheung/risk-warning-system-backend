package org.example.riskwarningsystembackend.module_system.dto;

import lombok.Data;
import org.example.riskwarningsystembackend.module_system.entity.Role;
import org.example.riskwarningsystembackend.module_system.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户详情数据传输对象，用于编辑回显
 */
@Data
public class UserDetailDto {
    private Long id;
    private String username;
    private String name;
    private String status;
    private Long organizationId;
    // 返回角色ID列表，以便前端多选框等组件回显
    private Set<Long> roleIds;

    public static UserDetailDto fromEntity(User user) {
        UserDetailDto dto = new UserDetailDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setStatus(user.getStatus());
        if (user.getOrganization() != null) {
            dto.setOrganizationId(user.getOrganization().getId());
        }
        if (user.getRoles() != null) {
            dto.setRoleIds(user.getRoles().stream().map(Role::getId).collect(Collectors.toSet()));
        }
        return dto;
    }
}