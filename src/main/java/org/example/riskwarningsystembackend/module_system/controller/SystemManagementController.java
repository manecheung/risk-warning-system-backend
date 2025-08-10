package org.example.riskwarningsystembackend.module_system.controller;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.common.dto.Result;
import org.example.riskwarningsystembackend.module_system.dto.*;
import org.example.riskwarningsystembackend.module_system.entity.Organization;
import org.example.riskwarningsystembackend.module_system.entity.Role;
import org.example.riskwarningsystembackend.module_system.entity.User;
import org.example.riskwarningsystembackend.module_system.service.SystemManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统管理模块控制器
 * 包含用户、角色、组织等管理功能。
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemManagementController {

    private final SystemManagementService systemManagementService;

    // --- 用户管理 ---
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ResponseEntity<Result<Map<String, Object>>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<User> userPage = systemManagementService.getUsers(pageable, keyword);
        Page<UserDto> dtoPage = userPage.map(UserDto::fromEntity);

        Map<String, Object> response = Map.of(
                "page", dtoPage.getNumber() + 1,
                "pageSize", dtoPage.getSize(),
                "totalRecords", dtoPage.getTotalElements(),
                "totalPages", dtoPage.getTotalPages(),
                "hasPrevPage", dtoPage.hasPrevious(),
                "hasNextPage", dtoPage.hasNext(),
                "records", dtoPage.getContent()
        );

        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public ResponseEntity<Result<UserDetailDto>> getUserById(@PathVariable Long id) {
        User user = systemManagementService.getUserById(id);
        return ResponseEntity.ok(Result.success(UserDetailDto.fromEntity(user)));
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public ResponseEntity<Result<UserDto>> createUser(@RequestBody UserCreateDto userCreateDto) {
        User newUser = systemManagementService.createUser(userCreateDto);
        return new ResponseEntity<>(Result.success(UserDto.fromEntity(newUser), "用户创建成功"), HttpStatus.CREATED);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public ResponseEntity<Result<UserDto>> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto userUpdateDto) {
        User updatedUser = systemManagementService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(Result.success(UserDto.fromEntity(updatedUser), "更新成功"));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public ResponseEntity<Result<Void>> deleteUser(@PathVariable Long id) {
        systemManagementService.deleteUser(id);
        return ResponseEntity.ok(Result.success(null, "删除成功"));
    }

    // --- 角色管理 ---
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ResponseEntity<Result<List<RoleDto>>> getRoles() {
        List<RoleDto> roles = systemManagementService.getAllRoles().stream()
                .map(RoleDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Result.success(roles));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ResponseEntity<Result<RoleDto>> createRole(@RequestBody RoleDto roleDto) {
        Role role = new Role();
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        Role newRole = systemManagementService.createRole(role);
        return new ResponseEntity<>(Result.success(RoleDto.fromEntity(newRole), "角色创建成功"), HttpStatus.CREATED);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ResponseEntity<Result<RoleDto>> updateRole(@PathVariable Long id, @RequestBody RoleDto roleDto) {
        Role roleDetails = new Role();
        roleDetails.setName(roleDto.getName());
        roleDetails.setDescription(roleDto.getDescription());
        Role updatedRole = systemManagementService.updateRole(id, roleDetails);
        return ResponseEntity.ok(Result.success(RoleDto.fromEntity(updatedRole), "更新成功"));
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ResponseEntity<Result<Void>> deleteRole(@PathVariable Long id) {
        systemManagementService.deleteRole(id);
        return ResponseEntity.ok(Result.success(null, "删除成功"));
    }

    @GetMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ResponseEntity<Result<RolePermissionDto>> getRolePermissions(@PathVariable Long id) {
        RolePermissionDto permissions = systemManagementService.getRolePermissions(id);
        return ResponseEntity.ok(Result.success(permissions));
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ResponseEntity<Result<Void>> updateRolePermissions(@PathVariable Long id, @RequestBody Map<String, List<String>> payload) {
        systemManagementService.updateRolePermissions(id, payload.get("permissionKeys"));
        return ResponseEntity.ok(Result.success(null, "权限更新成功"));
    }

    // --- 组织管理 ---
    /**
     * 7.3.1 获取组织架构树
     */
    @GetMapping("/organizations")
    @PreAuthorize("hasAuthority('system:org:list')")
    public ResponseEntity<Result<List<OrganizationDto>>> getOrganizations() {
        List<OrganizationDto> orgTree = systemManagementService.getOrganizationTree();
        return ResponseEntity.ok(Result.success(orgTree));
    }

    /**
     * 7.3.2 新增组织
     */
    @PostMapping("/organizations")
    @PreAuthorize("hasAuthority('system:org:edit')")
    public ResponseEntity<Result<OrganizationDto>> createOrganization(@RequestBody OrganizationCreateDto dto) {
        Organization newOrg = systemManagementService.createOrganization(dto);
        return new ResponseEntity<>(Result.success(OrganizationDto.fromEntity(newOrg), "组织创建成功"), HttpStatus.CREATED);
    }

    /**
     * 7.3.3 更新组织
     */
    @PutMapping("/organizations/{id}")
    @PreAuthorize("hasAuthority('system:org:edit')")
    public ResponseEntity<Result<OrganizationDto>> updateOrganization(@PathVariable Long id, @RequestBody OrganizationUpdateDto dto) {
        Organization updatedOrg = systemManagementService.updateOrganization(id, dto);
        return ResponseEntity.ok(Result.success(OrganizationDto.fromEntity(updatedOrg), "更新成功"));
    }

    /**
     * 7.3.4 删除组织
     */
    @DeleteMapping("/organizations/{id}")
    @PreAuthorize("hasAuthority('system:org:edit')")
    public ResponseEntity<Result<Void>> deleteOrganization(@PathVariable Long id) {
        systemManagementService.deleteOrganization(id);
        return ResponseEntity.ok(Result.success(null, "删除成功"));
    }
}
