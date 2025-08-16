package org.example.riskwarningsystembackend.dto.permission;

import lombok.Data;

import java.util.List;

/**
 * 权限数据传输对象
 * 用于封装权限相关的数据信息，包括已分配的权限键值和权限树结构
 */
@Data
public class PermissionDataDTO {
    /**
     * 已分配的权限键值列表
     * 存储用户或角色已被分配的权限标识符
     */
    private List<String> assignedKeys;

    /**
     * 权限树结构列表
     * 存储权限的层级结构信息，用于构建权限树形菜单或权限层级关系
     */
    private List<PermissionDTO> permissionTree;
}

