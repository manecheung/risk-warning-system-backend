package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.PermissionTreeDTO;
import org.example.riskwarningsystembackend.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 获取所有权限的树形结构
     * @return 权限树
     */
    @GetMapping
    public ResponseEntity<RestResult<List<PermissionTreeDTO>>> getAllPermissions() {
        List<PermissionTreeDTO> permissionTree = permissionService.getAllPermissionsAsTree();
        return ResponseEntity.ok(RestResult.success(permissionTree));
    }
}
