package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.CompanyListDTO;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDto;
import org.example.riskwarningsystembackend.dto.SupplyChainSummaryDTO;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.service.CompanyRelationService;
import org.example.riskwarningsystembackend.service.SupplyChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supply-chain")
public class SupplyChainController {

    private static final Logger logger = LoggerFactory.getLogger(SupplyChainController.class);

    private final SupplyChainService supplyChainService;
    private final CompanyRelationService companyRelationService;

    public SupplyChainController(SupplyChainService supplyChainService, CompanyRelationService companyRelationService) {
        this.supplyChainService = supplyChainService;
        this.companyRelationService = companyRelationService;
    }

    @GetMapping("/summary")
    public RestResult<SupplyChainSummaryDTO> getSummary() {
        return RestResult.success(supplyChainService.getSummary());
    }

    @GetMapping("/companies")
    public RestResult<PaginatedResponseDto<CompanyListDTO>> getCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        return RestResult.success(supplyChainService.getCompanies(keyword, pageRequest));
    }

    @GetMapping("/companies/{id}")
    public RestResult<CompanyInfo> getCompanyById(@PathVariable Long id) {
        CompanyInfo company = supplyChainService.getCompanyById(id);
        if (company != null) {
            return RestResult.success(company);
        } else {
            return RestResult.failure(404, "Company not found");
        }
    }

    @PostMapping("/companies")
    public RestResult<CompanyInfo> createCompany(@RequestBody CompanyInfo companyInfo) {
        CompanyInfo createdCompany = supplyChainService.createCompany(companyInfo);
        triggerRelationRebuild("company creation");
        return new RestResult<>(201, "创建成功", createdCompany);
    }

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

    @DeleteMapping("/companies/{id}")
    public RestResult<Void> deleteCompany(@PathVariable Long id) {
        supplyChainService.deleteCompany(id);
        triggerRelationRebuild("company deletion");
        return RestResult.success();
    }

    private void triggerRelationRebuild(String triggerSource) {
        try {
            logger.info("Triggering company relation rebuild due to: {}", triggerSource);
            companyRelationService.rebuildCompanyRelations();
        } catch (Exception e) {
            logger.error("Failed to rebuild company relations after {}. Error: {}", triggerSource, e.getMessage());
            // Do not rethrow, as the primary operation was successful.
        }
    }
}
