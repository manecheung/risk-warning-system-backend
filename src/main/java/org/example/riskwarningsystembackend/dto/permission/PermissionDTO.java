package org.example.riskwarningsystembackend.dto.permission;

import lombok.Data;
import java.util.List;

/**
 * 权限数据传输对象
 * 用于表示系统中的权限信息，支持树形结构的权限组织
 */
@Data
public class PermissionDTO {
    /**
     * 权限唯一标识键
     */
    private String key;

    /**
     * 权限显示标签
     */
    private String label;

    /**
     * 子权限列表，用于构建权限树形结构
     */
    private List<PermissionDTO> children;
}

