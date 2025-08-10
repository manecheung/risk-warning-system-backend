package org.example.riskwarningsystembackend.module_supply_chain.controller;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.common.dto.Result;
import org.example.riskwarningsystembackend.module_supply_chain.entity.Company;
import org.example.riskwarningsystembackend.module_supply_chain.service.SupplyChainService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 供应链管理模块控制器
 */
@RestController
@RequestMapping("/api/supply-chain")
@RequiredArgsConstructor
public class SupplyChainController {

    private final SupplyChainService supplyChainService;

    /**
     * 6.1 获取供应链风险概要
     */
    @GetMapping("/summary")
    public ResponseEntity<Result<Map<String, Object>>> getSummary() {
        return ResponseEntity.ok(Result.success(supplyChainService.getSummary()));
    }

    /**
     * 6.2 查询供应链企业列表
     */
    @GetMapping("/companies")
    public ResponseEntity<Result<Map<String, Object>>> getCompanies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Company> companyPage = supplyChainService.getCompanies(pageable, keyword);

        Map<String, Object> response = Map.of(
                "page", companyPage.getNumber() + 1,
                "pageSize", companyPage.getSize(),
                "totalRecords", companyPage.getTotalElements(),
                "totalPages", companyPage.getTotalPages(),
                "hasPrevPage", companyPage.hasPrevious(),
                "hasNextPage", companyPage.hasNext(),
                "records", companyPage.getContent()
        );
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 6.3 获取单个企业详情
     */
    @GetMapping("/companies/{id}")
    public ResponseEntity<Result<Company>> getCompanyById(@PathVariable Long id) {
        Company company = supplyChainService.getCompanyById(id);
        if (company != null) {
            return ResponseEntity.ok(Result.success(company));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 6.4 新增企业信息
     */
    @PostMapping("/companies")
    public ResponseEntity<Result<Company>> createCompany(@RequestBody Company company) {
        Company newCompany = supplyChainService.createCompany(company);
        return new ResponseEntity<>(Result.success(newCompany, "创建成功"), HttpStatus.CREATED);
    }

    /**
     * 6.5 更新企业信息
     */
    @PutMapping("/companies/{id}")
    public ResponseEntity<Result<Company>> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        Company updatedCompany = supplyChainService.updateCompany(id, company);
        return ResponseEntity.ok(Result.success(updatedCompany, "更新成功"));
    }

    /**
     * 6.6 删除企业信息
     */
    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Result<Void>> deleteCompany(@PathVariable Long id) {
        supplyChainService.deleteCompany(id);
        return ResponseEntity.ok(Result.success(null, "删除成功"));
    }
}
