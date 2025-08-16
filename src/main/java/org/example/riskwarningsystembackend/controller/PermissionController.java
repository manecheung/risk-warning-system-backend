package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.permission.PermissionTreeDTO;
import org.example.riskwarningsystembackend.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限控制器
 * 处理权限相关的HTTP请求
 */
@RestController
@RequestMapping("/api/system/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 构造函数
     *
     * @param permissionService 权限服务实例
     */
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 获取所有权限的树形结构
     *
     * @return 包含权限树数据的成功结果
     */
    @GetMapping
    public RestResult<List<PermissionTreeDTO>> getAllPermissions() {
        // 调用服务层获取权限树形结构
        List<PermissionTreeDTO> permissionTree = permissionService.getAllPermissionsAsTree();
        // 返回成功结果包装的权限树
        return RestResult.success(permissionTree);
    }
}
