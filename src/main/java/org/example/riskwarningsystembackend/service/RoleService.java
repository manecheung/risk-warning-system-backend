package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.*;
import org.example.riskwarningsystembackend.entity.Permission;
import org.example.riskwarningsystembackend.entity.Role;
import org.example.riskwarningsystembackend.repository.PermissionRepository;
import org.example.riskwarningsystembackend.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public Role createRole(RoleCreateDTO createDTO) {
        Role role = new Role();
        role.setName(createDTO.getName());
        role.setDescription(createDTO.getDescription());
        return roleRepository.save(role);
    }

    @Transactional
    public Optional<Role> updateRole(Long id, RoleUpdateDTO updateDTO) {
        return roleRepository.findById(id).map(role -> {
            role.setName(updateDTO.getName());
            role.setDescription(updateDTO.getDescription());
            return roleRepository.save(role);
        });
    }

    @Transactional
    public boolean deleteRole(Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public PermissionDataDTO getPermissionsByRoleId(Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        List<String> assignedKeys = role.getPermissions().stream().map(Permission::getKey).collect(Collectors.toList());

        List<Permission> allPermissions = permissionRepository.findAll();
        List<PermissionDTO> permissionTree = buildPermissionTree(allPermissions, null);

        PermissionDataDTO permissionDataDTO = new PermissionDataDTO();
        permissionDataDTO.setAssignedKeys(assignedKeys);
        permissionDataDTO.setPermissionTree(permissionTree);
        return permissionDataDTO;
    }

    @Transactional
    public void updateRolePermissions(Long roleId, List<String> permissionKeys) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Permission> permissions = permissionRepository.findByKeyIn(permissionKeys);
        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    private List<PermissionDTO> buildPermissionTree(List<Permission> permissions, Long parentId) {
        return permissions.stream()
                .filter(p -> (parentId == null && p.getParentId() == null) || (parentId != null && parentId.equals(p.getParentId())))
                .map(p -> {
                    PermissionDTO dto = new PermissionDTO();
                    dto.setKey(p.getKey());
                    dto.setLabel(p.getLabel());
                    dto.setChildren(buildPermissionTree(permissions, p.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private RoleDTO convertToDto(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }
}
