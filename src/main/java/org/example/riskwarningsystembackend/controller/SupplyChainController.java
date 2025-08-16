package org.example.riskwarningsystembackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDTO;
import org.example.riskwarningsystembackend.dto.supplychain.CompanyListDTO;
import org.example.riskwarningsystembackend.dto.supplychain.SupplyChainSummaryDTO;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.service.DataInitial.CompanyRelationService;
import org.example.riskwarningsystembackend.service.SupplyChainService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应链相关接口控制器，提供公司信息的增删改查、分页查询、统计摘要等功能。
 */
@Slf4j
@RestController
@RequestMapping("/api/supply-chain")
public class SupplyChainController {

    private final SupplyChainService supplyChainService;
    private final CompanyRelationService companyRelationService;

    /**
     * 构造方法注入依赖的服务类。
     *
     * @param supplyChainService     供应链服务类，用于处理公司信息相关的业务逻辑
     * @param companyRelationService 公司关系服务类，用于重建公司之间的关联关系
     */
    public SupplyChainController(SupplyChainService supplyChainService, CompanyRelationService companyRelationService) {
        this.supplyChainService = supplyChainService;
        this.companyRelationService = companyRelationService;
    }

    /**
     * 获取所有公司信息列表。
     *
     * @return 包含所有公司信息的成功响应结果
     */
    @GetMapping("/all-companies")
    public RestResult<List<CompanyInfo>> getAllCompanies() {
        return RestResult.success(supplyChainService.getAllCompanies());
    }

    /**
     * 获取供应链系统的统计摘要信息。
     *
     * @return 包含供应链系统统计摘要的成功响应结果
     */
    @GetMapping("/summary")
    public RestResult<SupplyChainSummaryDTO> getSummary() {
        return RestResult.success(supplyChainService.getSummary());
    }

    /**
     * 分页获取公司列表，支持关键词搜索。
     *
     * @param keyword  搜索关键词（可选）
     * @param page     当前页码，默认为1
     * @param pageSize 每页数据量，默认为10
     * @return 分页后的公司列表数据及分页信息
     */
    @GetMapping("/companies")
    public RestResult<PaginatedResponseDTO<CompanyListDTO>> getCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        return RestResult.success(supplyChainService.getCompanies(keyword, pageRequest));
    }

    /**
     * 根据ID获取单个公司信息。
     *
     * @param id 公司ID
     * @return 如果找到对应公司则返回公司信息，否则返回404错误
     */
    @GetMapping("/companies/{id}")
    public RestResult<CompanyInfo> getCompanyById(@PathVariable Long id) {
        CompanyInfo company = supplyChainService.getCompanyById(id);
        if (company != null) {
            return RestResult.success(company);
        } else {
            return RestResult.failure(404, "Company not found");
        }
    }

    /**
     * 创建新的公司信息。
     *
     * @param companyInfo 待创建的公司信息对象
     * @return 创建成功的公司信息和成功状态码201
     */
    @PostMapping("/companies")
    public RestResult<CompanyInfo> createCompany(@RequestBody CompanyInfo companyInfo) {
        CompanyInfo createdCompany = supplyChainService.createCompany(companyInfo);
        triggerRelationRebuild("company creation");
        return new RestResult<>(201, "创建成功", createdCompany);
    }

    /**
     * 更新指定ID的公司信息。
     *
     * @param id             公司ID
     * @param companyDetails 更新后的公司信息对象
     * @return 如果更新成功则返回更新后的公司信息，否则返回404错误
     */
    @PutMapping("/companies/{id}")
    public RestResult<CompanyInfo> updateCompany(@PathVariable Long id, @RequestBody CompanyInfo companyDetails) {
        CompanyInfo updatedCompany = supplyChainService.updateCompany(id, companyDetails);
        if (updatedCompany != null) {
            triggerRelationRebuild("company update");
            return RestResult.success(updatedCompany);
        } else {
            return RestResult.failure(404, "Company not found");
        }
    }

    /**
     * 删除指定ID的公司信息。
     *
     * @param id 要删除的公司ID
     * @return 删除成功的结果
     */
    @DeleteMapping("/companies/{id}")
    public RestResult<Void> deleteCompany(@PathVariable Long id) {
        supplyChainService.deleteCompany(id);
        triggerRelationRebuild("company deletion");
        return RestResult.success();
    }

    /**
     * 在公司信息发生变更时触发公司关系的重建操作。
     * <p>
     * 此方法会在公司创建、更新或删除后被调用，以确保公司之间的关联关系是最新的。
     * 如果重建过程中出现异常，仅记录日志而不中断主流程。
     *
     * @param triggerSource 触发重建的原因描述，如 "company creation"、"company update" 等
     */
    private void triggerRelationRebuild(String triggerSource) {
        try {
            log.info("触发公司关系重建: {}", triggerSource);
            companyRelationService.rebuildCompanyRelations();
        } catch (Exception e) {
            log.error("未能重建公司关系 {}. 原因: {}", triggerSource, e.getMessage());
            // 不重新抛出异常，因为主要操作已成功执行。
        }
    }
}
