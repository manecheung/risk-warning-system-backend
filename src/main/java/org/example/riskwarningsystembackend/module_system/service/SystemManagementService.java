package org.example.riskwarningsystembackend.module_system.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_system.dto.*;
import org.example.riskwarningsystembackend.module_system.entity.Organization;
import org.example.riskwarningsystembackend.module_system.entity.Permission;
import org.example.riskwarningsystembackend.module_system.entity.Role;
import org.example.riskwarningsystembackend.module_system.entity.User;
import org.example.riskwarningsystembackend.module_system.repository.OrganizationRepository;
import org.example.riskwarningsystembackend.module_system.repository.PermissionRepository;
import org.example.riskwarningsystembackend.module_system.repository.RoleRepository;
import org.example.riskwarningsystembackend.module_system.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统管理服务
 * 负责处理用户、角色、组织等核心实体的业务逻辑。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SystemManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    // --- 用户管理 ---

    @Transactional(readOnly = true)
    public Page<User> getUsers(Pageable pageable, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.findByKeywordWithDetails(keyword, pageable);
        }
        return userRepository.findAllWithDetails(pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + id + " 的用户"));
    }

    public User createUser(UserCreateDto userCreateDto) {
        if (userRepository.findByUsername(userCreateDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名 " + userCreateDto.getUsername() + " 已存在");
        }

        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setName(userCreateDto.getName());
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        user.setStatus(userCreateDto.getStatus());

        if (userCreateDto.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(userCreateDto.getOrganizationId())
                    .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + userCreateDto.getOrganizationId() + " 的组织"));
            user.setOrganization(org);
        }

        if (userCreateDto.getRoleIds() != null && !userCreateDto.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(userCreateDto.getRoleIds()));
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = getUserById(id);
        user.setName(userUpdateDto.getName());
        user.setStatus(userUpdateDto.getStatus());

        if (StringUtils.hasText(userUpdateDto.getPassword())) {
            user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }

        if (userUpdateDto.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(userUpdateDto.getOrganizationId())
                    .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + userUpdateDto.getOrganizationId() + " 的组织"));
            user.setOrganization(org);
        } else {
            user.setOrganization(null);
        }

        if (userUpdateDto.getRoleIds() != null && !userUpdateDto.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(userUpdateDto.getRoleIds()));
            user.setRoles(roles);
        } else {
            user.getRoles().clear();
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // --- 角色管理 ---

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role roleDetails) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + id + " 的角色"));
        role.setName(roleDetails.getName());
        role.setDescription(roleDetails.getDescription());
        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public RolePermissionDto getRolePermissions(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + id + " 的角色"));
        List<String> assignedKeys = role.getPermissions().stream()
                .map(Permission::getPermissionKey)
                .collect(Collectors.toList());

        List<PermissionDto> permissionTree = buildPermissionTree();

        return new RolePermissionDto(assignedKeys, permissionTree);
    }

    public void updateRolePermissions(Long id, List<String> permissionKeys) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + id + " 的角色"));
        Set<Permission> permissions = permissionRepository.findByPermissionKeyIn(permissionKeys);
        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    /**
     * 构建权限树。
     * 该方法会解析权限标识符（如 'system:user:list'）来构建层级关系。
     * @return 权限DTO的树形列表
     */
    private List<PermissionDto> buildPermissionTree() {
        List<Permission> allPermissions = permissionRepository.findAll();
        Map<String, PermissionDto> nodes = new LinkedHashMap<>();

        // 1. 为数据库中存在的所有权限创建基础节点
        for (Permission p : allPermissions) {
            nodes.put(p.getPermissionKey(), new PermissionDto(p.getPermissionKey(), p.getName(), new ArrayList<>()));
        }

        // 2. 动态创建不存在的父节点占位符
        // 例如，对于 "system:user:list"，如果 "system:user" 和 "system" 不作为具体权限存在，也要创建它们作为父节点
        for (Permission p : allPermissions) {
            String key = p.getPermissionKey();
            while (key.contains(":")) {
                key = key.substring(0, key.lastIndexOf(':'));
                if (!nodes.containsKey(key)) {
                    String label = key.substring(key.lastIndexOf(':') + 1);
                    // 将首字母大写作为父节点的显示名称，例如 "user" -> "User"
                    label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
                    // 为父节点创建一个临时的DTO
                    nodes.put(key, new PermissionDto(key, label, new ArrayList<>()));
                }
            }
        }

        // 3. 链接父子关系
        for (PermissionDto node : nodes.values()) {
            String key = node.getKey();
            if (key.contains(":")) {
                String parentKey = key.substring(0, key.lastIndexOf(':'));
                if (nodes.containsKey(parentKey)) {
                    nodes.get(parentKey).getChildren().add(node);
                }
            }
        }

        // 4. 收集所有根节点（即不包含":"的顶级节点），并返回
        return nodes.values().stream()
                .filter(node -> !node.getKey().contains(":"))
                .collect(Collectors.toList());
    }

    // --- 组织管理 ---

    /**
     * 获取组织架构树
     * @return 组织架构的树形结构列表
     */
    @Transactional(readOnly = true)
    public List<OrganizationDto> getOrganizationTree() {
        List<Organization> allOrgs = organizationRepository.findAll();
        Map<Long, OrganizationDto> dtoMap = allOrgs.stream()
                .map(OrganizationDto::fromEntity)
                .collect(Collectors.toMap(OrganizationDto::getId, dto -> dto));

        List<OrganizationDto> rootNodes = new ArrayList<>();
        for (Organization org : allOrgs) {
            OrganizationDto dto = dtoMap.get(org.getId());
            if (org.getParent() != null) {
                OrganizationDto parentDto = dtoMap.get(org.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            } else {
                rootNodes.add(dto);
            }
        }
        return rootNodes;
    }

    /**
     * 创建新组织
     * @param dto 创建组织所需的数据
     * @return 创建后的组织实体
     */
    public Organization createOrganization(OrganizationCreateDto dto) {
        Organization org = new Organization();
        org.setName(dto.getName());
        org.setManager(dto.getManager());

        if (dto.getParentId() != null) {
            Organization parent = organizationRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + dto.getParentId() + " 的父组织"));
            org.setParent(parent);
        }
        return organizationRepository.save(org);
    }

    /**
     * 更新组织信息
     * @param id 要更新的组织ID
     * @param dto 更新数据
     * @return 更新后的组织实体
     */
    public Organization updateOrganization(Long id, OrganizationUpdateDto dto) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + id + " 的组织"));

        org.setName(dto.getName());
        org.setManager(dto.getManager());

        if (dto.getParentId() != null) {
            // 防止将组织设置为自己的子节点
            if (id.equals(dto.getParentId())) {
                throw new IllegalArgumentException("不能将组织设置为自己的子组织");
            }
            Organization parent = organizationRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + dto.getParentId() + " 的父组织"));
            org.setParent(parent);
        } else {
            org.setParent(null);
        }

        return organizationRepository.save(org);
    }

    /**
     * 删除组织
     * @param id 要删除的组织ID
     */
    public void deleteOrganization(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + id + " 的组织"));

        // 根据设计文档，删除前检查依赖
        if (!org.getChildren().isEmpty()) {
            throw new IllegalStateException("无法删除，该组织下存在子组织");
        }
        if (!org.getUsers().isEmpty()) {
            throw new IllegalStateException("无法删除，该组织下还存在用户");
        }

        organizationRepository.deleteById(id);
    }
}

