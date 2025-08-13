package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.PermissionTreeDTO;
import org.example.riskwarningsystembackend.entity.Permission;
import org.example.riskwarningsystembackend.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionTreeDTO> getAllPermissionsAsTree() {
        List<Permission> allPermissions = permissionRepository.findAll();
        Map<Long, List<Permission>> parentIdToChildrenMap = allPermissions.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId));

        return allPermissions.stream()
                .filter(p -> p.getParentId() == null) // 从根节点开始
                .map(p -> buildTree(p, parentIdToChildrenMap))
                .collect(Collectors.toList());
    }

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