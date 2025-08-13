package org.example.riskwarningsystembackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.riskwarningsystembackend.entity.Permission;

import java.util.List;

@Data
@NoArgsConstructor
public class PermissionTreeDTO {
    private Long id;
    private String key;
    private String label;
    private List<PermissionTreeDTO> children;

    public PermissionTreeDTO(Permission permission) {
        this.id = permission.getId();
        this.key = permission.getKey();
        this.label = permission.getLabel();
    }
}
