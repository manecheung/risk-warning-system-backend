package org.example.riskwarningsystembackend.dto.permission;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.riskwarningsystembackend.entity.Permission;

import java.util.List;

/**
 * 权限树DTO类，用于构建权限树形结构
 */
@Data
@NoArgsConstructor
public class PermissionTreeDTO {
    private Long id;
    private String key;
    private String label;
    private List<PermissionTreeDTO> children;

    /**
     * 通过Permission实体对象构造PermissionTreeDTO对象
     *
     * @param permission 权限实体对象，用于初始化DTO的属性值
     */
    public PermissionTreeDTO(Permission permission) {
        this.id = permission.getId();
        this.key = permission.getKey();
        this.label = permission.getLabel();
    }
}

