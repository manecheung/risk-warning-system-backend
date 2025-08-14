package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.*;
import org.example.riskwarningsystembackend.entity.Role;
import org.example.riskwarningsystembackend.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

        @GetMapping
    public RestResult<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return RestResult.success(roles);
    }

    @PostMapping
    public ResponseEntity<RestResult<Map<String, Long>>> createRole(@RequestBody RoleCreateDTO createDTO) {
        Role newRole = roleService.createRole(createDTO);
        return new ResponseEntity<>(new RestResult<>(ResultCode.CREATED.getCode(), ResultCode.CREATED.getMessage(), Map.of("id", newRole.getId())), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestResult<Void>> updateRole(@PathVariable Long id, @RequestBody RoleUpdateDTO updateDTO) {
        return roleService.updateRole(id, updateDTO)
                .map(role -> ResponseEntity.ok(RestResult.<Void>success()))
                .orElseGet(() -> new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResult<Void>> deleteRole(@PathVariable Long id) {
        boolean deleted = roleService.deleteRole(id);
        if (deleted) {
            return ResponseEntity.ok(RestResult.success());
        } else {
            return new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/permissions")
    public RestResult<PermissionDataDTO> getRolePermissions(@PathVariable Long id) {
        PermissionDataDTO permissions = roleService.getPermissionsByRoleId(id);
        return RestResult.success(permissions);
    }

    @PutMapping("/{id}/permissions")
    public RestResult<Void> updateRolePermissions(@PathVariable Long id, @RequestBody PermissionsUpdateRequestDTO request) {
        roleService.updateRolePermissions(id, request.getPermissionKeys());
        return RestResult.success();
    }
}
