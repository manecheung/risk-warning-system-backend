package org.example.riskwarningsystembackend.service;

import jakarta.persistence.criteria.Predicate;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDto;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Transactional(readOnly = true)
    public PaginatedResponseDto<UserDTO> getUsers(Pageable pageable, String keyword) {
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

        return new PaginatedResponseDto<>(
                userPage.getNumber() + 1,
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasPrevious(),
                userPage.hasNext(),
                userDTOs
        );
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToDto);
    }

    @Transactional
    public User createUser(UserCreateDTO createDTO) {
        User user = new User();
        user.setUsername(createDTO.getUsername());
        user.setName(createDTO.getName());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword())); // 使用加密器加密密码
        user.setStatus(createDTO.getStatus());
        user.setLastLogin(LocalDateTime.now());

        Role role = roleRepository.findById(createDTO.getRoleId()).orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);

        Organization organization = organizationRepository.findById(createDTO.getOrganizationId()).orElseThrow(() -> new RuntimeException("Organization not found"));
        user.setOrganization(organization);

        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> updateUser(Long id, UserUpdateDTO updateDTO) {
        return userRepository.findById(id).map(user -> {
            user.setName(updateDTO.getName());
            user.setStatus(updateDTO.getStatus());

            Role role = roleRepository.findById(updateDTO.getRoleId()).orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);

            Organization organization = organizationRepository.findById(updateDTO.getOrganizationId()).orElseThrow(() -> new RuntimeException("Organization not found"));
            user.setOrganization(organization);

            return userRepository.save(user);
        });
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setRole(user.getRole() != null ? user.getRole().getName() : null);
        dto.setOrganization(user.getOrganization() != null ? user.getOrganization().getName() : null);
        dto.setStatus(user.getStatus());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }
}
