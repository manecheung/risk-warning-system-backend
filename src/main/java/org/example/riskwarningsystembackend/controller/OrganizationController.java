package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.OrganizationCreateDTO;
import org.example.riskwarningsystembackend.dto.OrganizationTreeDTO;
import org.example.riskwarningsystembackend.dto.OrganizationUpdateDTO;
import org.example.riskwarningsystembackend.entity.Organization;
import org.example.riskwarningsystembackend.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * 获取组织架构树
     * @return 组织架构的层级列表
     */
    @GetMapping
    public ResponseEntity<RestResult<List<OrganizationTreeDTO>>> getOrganizationTree() {
        List<OrganizationTreeDTO> tree = organizationService.getOrganizationTree();
        return ResponseEntity.ok(RestResult.success(tree));
    }

    /**
     * 新增组织
     * @param createDTO 新增组织的数据传输对象
     * @return 新创建的组织的ID
     */
    @PostMapping
    public ResponseEntity<RestResult<Map<String, Long>>> createOrganization(@RequestBody OrganizationCreateDTO createDTO) {
        Organization newOrg = organizationService.createOrganization(createDTO);
        return new ResponseEntity<>(new RestResult<>(ResultCode.CREATED.getCode(), ResultCode.CREATED.getMessage(), Map.of("id", newOrg.getId())), HttpStatus.CREATED);
    }

    /**
     * 更新组织信息
     * @param id 组织的ID
     * @param updateDTO 更新组织的数据传输对象
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestResult<Void>> updateOrganization(@PathVariable Long id, @RequestBody OrganizationUpdateDTO updateDTO) {
        return organizationService.updateOrganization(id, updateDTO)
                .map(org -> ResponseEntity.ok(RestResult.<Void>success()))
                .orElseGet(() -> new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND));
    }

    /**
     * 删除组织
     * @param id 组织的ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResult<Void>> deleteOrganization(@PathVariable Long id) {
        try {
            organizationService.deleteOrganization(id);
            return ResponseEntity.ok(RestResult.success());
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(RestResult.failure(ResultCode.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
