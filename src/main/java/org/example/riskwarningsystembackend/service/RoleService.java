package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.permission.PermissionDTO;
import org.example.riskwarningsystembackend.dto.permission.PermissionDataDTO;
import org.example.riskwarningsystembackend.dto.role.RoleCreateDTO;
import org.example.riskwarningsystembackend.dto.role.RoleDTO;
import org.example.riskwarningsystembackend.dto.role.RoleUpdateDTO;
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

/**
 * 角色服务类，提供角色的增删改查以及权限分配功能。
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    /**
     * 构造方法，注入角色和权限的持久层仓库。
     *
     * @param roleRepository       角色数据访问对象
     * @param permissionRepository 权限数据访问对象
     */
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * 获取所有角色信息，并转换为 DTO 格式返回。
     *
     * @return 所有角色的 DTO 列表
     */
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 创建一个新的角色。
     *
     * @param createDTO 包含角色名称和描述的创建数据传输对象
     * @return 创建成功的角色实体
     */
    @Transactional
    public Role createRole(RoleCreateDTO createDTO) {
        Role role = new Role();
        role.setName(createDTO.getName());
        role.setDescription(createDTO.getDescription());
        return roleRepository.save(role);
    }

    /**
     * 更新指定 ID 的角色信息。
     *
     * @param id        要更新的角色 ID
     * @param updateDTO 包含新名称和描述的更新数据传输对象
     * @return 更新后的角色实体，如果角色不存在则返回空 Optional
     */
    @Transactional
    public Optional<Role> updateRole(Long id, RoleUpdateDTO updateDTO) {
        return roleRepository.findById(id).map(role -> {
            role.setName(updateDTO.getName());
            role.setDescription(updateDTO.getDescription());
            return roleRepository.save(role);
        });
    }

    /**
     * 删除指定 ID 的角色。
     *
     * @param id 要删除的角色 ID
     * @return 如果删除成功返回 true，否则返回 false
     */
    @Transactional
    public boolean deleteRole(Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 获取指定角色 ID 的权限信息，包括已分配权限键和完整的权限树结构。
     *
     * @param roleId 角色 ID
     * @return 权限数据传输对象，包含已分配权限键和权限树
     */
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

    /**
     * 更新指定角色的权限集合。
     *
     * @param roleId         角色 ID
     * @param permissionKeys 新的权限键列表
     */
    @Transactional
    public void updateRolePermissions(Long roleId, List<String> permissionKeys) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Permission> permissions = new java.util.HashSet<>(permissionRepository.findByKeyIn(permissionKeys));
        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    /**
     * 构建权限树结构，递归生成父子级关系的权限 DTO 列表。
     *
     * @param permissions 所有权限列表
     * @param parentId    父级权限 ID，null 表示顶级权限
     * @return 权限树结构的 DTO 列表
     */
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

    /**
     * 将 Role 实体转换为 RoleDTO 对象。
     *
     * @param role 角色实体
     * @return 转换后的角色 DTO 对象
     */
    private RoleDTO convertToDto(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }
}
