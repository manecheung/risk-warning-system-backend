package org.example.riskwarningsystembackend.module_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 权限树节点数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {
    private String key;
    private String label;
    private List<PermissionDto> children;
}
