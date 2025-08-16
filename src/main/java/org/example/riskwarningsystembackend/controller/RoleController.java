package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.permission.PermissionDataDTO;
import org.example.riskwarningsystembackend.dto.permission.PermissionsUpdateRequestDTO;
import org.example.riskwarningsystembackend.dto.role.RoleCreateDTO;
import org.example.riskwarningsystembackend.dto.role.RoleDTO;
import org.example.riskwarningsystembackend.dto.role.RoleUpdateDTO;
import org.example.riskwarningsystembackend.entity.Role;
import org.example.riskwarningsystembackend.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器，提供角色的增删改查以及权限分配功能。
 */
@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    private final RoleService roleService;

    /**
     * 构造函数注入角色服务。
     *
     * @param roleService 角色业务逻辑处理服务
     */
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 获取所有角色信息。
     *
     * @return 包含所有角色数据的成功结果
     */
    @GetMapping
    public RestResult<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return RestResult.success(roles);
    }

    /**
     * 创建一个新的角色。
     *
     * @param createDTO 角色创建请求数据传输对象
     * @return 包含新创建角色ID的成功响应实体
     */
    @PostMapping
    public ResponseEntity<RestResult<Map<String, Long>>> createRole(@RequestBody RoleCreateDTO createDTO) {
        Role newRole = roleService.createRole(createDTO);
        return new ResponseEntity<>(new RestResult<>(ResultCode.CREATED.getCode(), ResultCode.CREATED.getMessage(), Map.of("id", newRole.getId())), HttpStatus.CREATED);
    }

    /**
     * 更新指定ID的角色信息。
     *
     * @param id        要更新的角色ID
     * @param updateDTO 角色更新请求数据传输对象
     * @return 更新成功返回空内容的成功响应，否则返回404未找到响应
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestResult<Void>> updateRole(@PathVariable Long id, @RequestBody RoleUpdateDTO updateDTO) {
        return roleService.updateRole(id, updateDTO)
                .map(role -> ResponseEntity.ok(RestResult.<Void>success()))
                .orElseGet(() -> new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND));
    }

    /**
     * 删除指定ID的角色。
     *
     * @param id 要删除的角色ID
     * @return 删除成功返回空内容的成功响应，否则返回404未找到响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResult<Void>> deleteRole(@PathVariable Long id) {
        boolean deleted = roleService.deleteRole(id);
        if (deleted) {
            return ResponseEntity.ok(RestResult.success());
        } else {
            return new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 获取指定角色的权限信息。
     *
     * @param id 角色ID
     * @return 包含该角色权限信息的成功结果
     */
    @GetMapping("/{id}/permissions")
    public RestResult<PermissionDataDTO> getRolePermissions(@PathVariable Long id) {
        PermissionDataDTO permissions = roleService.getPermissionsByRoleId(id);
        return RestResult.success(permissions);
    }

    /**
     * 更新指定角色的权限列表。
     *
     * @param id      角色ID
     * @param request 权限更新请求数据传输对象，包含权限键列表
     * @return 更新成功的空内容结果
     */
    @PutMapping("/{id}/permissions")
    public RestResult<Void> updateRolePermissions(@PathVariable Long id, @RequestBody PermissionsUpdateRequestDTO request) {
        roleService.updateRolePermissions(id, request.getPermissionKeys());
        return RestResult.success();
    }
}
