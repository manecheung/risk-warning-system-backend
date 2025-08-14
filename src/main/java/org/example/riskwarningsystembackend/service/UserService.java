package org.example.riskwarningsystembackend.service;

import jakarta.persistence.criteria.Predicate;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDTO;
import org.example.riskwarningsystembackend.dto.UserCreateDTO;
import org.example.riskwarningsystembackend.dto.UserDTO;
import org.example.riskwarningsystembackend.dto.UserUpdateDTO;
import org.example.riskwarningsystembackend.entity.Organization;
import org.example.riskwarningsystembackend.entity.Role;
import org.example.riskwarningsystembackend.entity.User;
import org.example.riskwarningsystembackend.repository.OrganizationRepository;
import org.example.riskwarningsystembackend.repository.RoleRepository;
import org.example.riskwarningsystembackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@code @description}  用户管理服务
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * {@code @description}  分页获取用户列表
     * @param pageable 分页参数
     * @param keyword 搜索关键字
     * @return 分页的用户数据
     */
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<UserDTO> getUsers(Pageable pageable, String keyword) {
        Specification<User> spec = (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(root.get("username"), "%" + keyword + "%"));
            predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
            return cb.or(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<UserDTO> userDTOs = userPage.getContent().stream().map(this::convertToDto).toList();

        return new PaginatedResponseDTO<>(
                userPage.getNumber() + 1,
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasPrevious(),
                userPage.hasNext(),
                userDTOs
        );
    }

    /**
     * {@code @description}  根据ID获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToDto);
    }

    /**
     * {@code @description}  创建新用户
     * @param createDTO 用户创建DTO
     * @return 创建的用户实体
     */
    @Transactional
    public User createUser(UserCreateDTO createDTO) {
        User user = new User();
        user.setUsername(createDTO.getUsername());
        user.setName(createDTO.getName());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setStatus(createDTO.getStatus());
        user.setLastLogin(LocalDateTime.now());

        updateUserRoles(user, createDTO.getRoleIds());

        if (createDTO.getOrganizationId() != null) {
            Organization organization = organizationRepository.findById(createDTO.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Organization not found with id: " + createDTO.getOrganizationId()));
            user.setOrganization(organization);
        }

        return userRepository.save(user);
    }

    /**
     * {@code @description} 更新用户信息
     * @param id 用户ID
     * @param updateDTO 用户更新DTO
     * @return 更新后的用户实体
     */
    @Transactional
    public Optional<User> updateUser(Long id, UserUpdateDTO updateDTO) {
        return userRepository.findById(id).map(user -> {
            user.setName(updateDTO.getName());
            user.setStatus(updateDTO.getStatus());

            // 如果密码字段不为空，则更新密码
            if (StringUtils.hasText(updateDTO.getPassword())) {
                user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
            }

            updateUserRoles(user, updateDTO.getRoleIds());

            if (updateDTO.getOrganizationId() != null) {
                Organization organization = organizationRepository.findById(updateDTO.getOrganizationId())
                        .orElseThrow(() -> new RuntimeException("Organization not found with id: " + updateDTO.getOrganizationId()));
                user.setOrganization(organization);
            } else {
                user.setOrganization(null);
            }

            return userRepository.save(user);
        });
    }

    /**
     * {@code @description} 删除用户
     * @param id 用户ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * {@code @description} 更新用户的角色
     * @param user 用户实体
     * @param roleIds 角色ID集合
     */
    private void updateUserRoles(User user, Set<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            user.setRoles(new HashSet<>());
        } else {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            if (roles.size() != roleIds.size()) {
                throw new RuntimeException("One or more roles not found");
            }
            user.setRoles(roles);
        }
    }

    /**
     * {@code @description} 将用户实体转换为DTO
     * @param user 用户实体
     * @return 用户DTO
     */
    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        }
        dto.setOrganization(user.getOrganization() != null ? user.getOrganization().getName() : null);
        dto.setStatus(user.getStatus());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }
}