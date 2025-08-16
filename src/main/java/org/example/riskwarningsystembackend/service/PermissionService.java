package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.permission.PermissionTreeDTO;
import org.example.riskwarningsystembackend.entity.Permission;
import org.example.riskwarningsystembackend.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务类，提供权限相关的业务逻辑处理
 */
@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * 获取所有权限并构建成树形结构
     *
     * @return 权限树形结构列表，每个元素代表一个根权限节点及其子权限
     */
    @Transactional(readOnly = true)
    public List<PermissionTreeDTO> getAllPermissionsAsTree() {
        List<Permission> allPermissions = permissionRepository.findAll();
        // 按父ID分组，构建父权限ID到子权限列表的映射关系
        Map<Long, List<Permission>> parentIdToChildrenMap = allPermissions.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId));

        // 从根节点开始构建权限树
        return allPermissions.stream()
                .filter(p -> p.getParentId() == null) // 从根节点开始
                .map(p -> buildTree(p, parentIdToChildrenMap))
                .collect(Collectors.toList());
    }

    /**
     * 递归构建权限树形结构
     *
     * @param permission 当前权限节点
     * @param map 父权限ID到子权限列表的映射关系
     * @return 构建好的权限树DTO对象
     */
    private PermissionTreeDTO buildTree(Permission permission, Map<Long, List<Permission>> map) {
        PermissionTreeDTO dto = new PermissionTreeDTO(permission);
        List<Permission> children = map.get(permission.getId());
        if (children != null && !children.isEmpty()) {
            dto.setChildren(children.stream()
                    .map(child -> buildTree(child, map))
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
