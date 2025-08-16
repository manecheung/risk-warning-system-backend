package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.organization.OrganizationCreateDTO;
import org.example.riskwarningsystembackend.dto.organization.OrganizationTreeDTO;
import org.example.riskwarningsystembackend.dto.organization.OrganizationUpdateDTO;
import org.example.riskwarningsystembackend.entity.Organization;
import org.example.riskwarningsystembackend.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 组织管理控制器，提供组织的增删改查接口
 */
@RestController
@RequestMapping("/api/system/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * 构造函数注入组织服务
     *
     * @param organizationService 组织相关业务逻辑的服务类
     */
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * 获取组织架构树
     *
     * @return 组织架构的层级列表
     */
    @GetMapping
    public RestResult<List<OrganizationTreeDTO>> getOrganizationTree() {
        List<OrganizationTreeDTO> tree = organizationService.getOrganizationTree();
        return RestResult.success(tree);
    }

    /**
     * 新增组织
     *
     * @param createDTO 新增组织的数据传输对象，包含组织名称、父级ID等信息
     * @return 包含新创建组织ID的成功响应实体
     */
    @PostMapping
    public ResponseEntity<RestResult<Map<String, Long>>> createOrganization(@RequestBody OrganizationCreateDTO createDTO) {
        Organization newOrg = organizationService.createOrganization(createDTO);
        return new ResponseEntity<>(new RestResult<>(ResultCode.CREATED.getCode(), ResultCode.CREATED.getMessage(), Map.of("id", newOrg.getId())), HttpStatus.CREATED);
    }

    /**
     * 更新组织信息
     *
     * @param id        组织的唯一标识符
     * @param updateDTO 更新组织的数据传输对象，包含需要更新的字段
     * @return 更新成功返回空内容的成功响应；若组织不存在则返回404状态码
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestResult<Void>> updateOrganization(@PathVariable Long id, @RequestBody OrganizationUpdateDTO updateDTO) {
        // 根据更新结果判断返回成功或失败响应
        return organizationService.updateOrganization(id, updateDTO)
                .map(org -> ResponseEntity.ok(RestResult.<Void>success()))
                .orElseGet(() -> new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND));
    }

    /**
     * 删除指定ID的组织
     *
     * @param id 要删除的组织的唯一标识符
     * @return 删除成功返回空内容的成功响应；若因业务规则无法删除则返回错误信息及400状态码
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResult<Void>> deleteOrganization(@PathVariable Long id) {
        try {
            organizationService.deleteOrganization(id);
            return ResponseEntity.ok(RestResult.success());
        } catch (IllegalStateException e) {
            // 捕获非法状态异常，如存在子组织或关联用户时不允许删除
            return new ResponseEntity<>(RestResult.failure(ResultCode.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
