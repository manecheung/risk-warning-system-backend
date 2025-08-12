package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.*;
import org.example.riskwarningsystembackend.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/key-metrics")
    public RestResult<List<KeyMetricDTO>> getKeyMetrics() {
        return RestResult.success(dashboardService.getKeyMetrics());
    }

    @GetMapping("/risk-distribution")
    public RestResult<List<RiskDistributionDTO>> getRiskDistribution() {
        return RestResult.success(dashboardService.getRiskDistribution());
    }

    @GetMapping("/industry-health")
    public RestResult<IndustryHealthDTO> getIndustryHealth() {
        return RestResult.success(dashboardService.getIndustryHealth());
    }

    @GetMapping("/supply-chain-risk")
    public RestResult<SupplyChainRiskDTO> getSupplyChainRisk() {
        return RestResult.success(dashboardService.getSupplyChainRisk());
    }

    @GetMapping("/risk-analysis")
    public RestResult<PaginatedResponseDto<RiskAnalysisDto>> getRiskAnalysis(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        return RestResult.success(dashboardService.getRiskAnalysis(pageRequest));
    }

    @GetMapping("/risk-map")
    public RestResult<List<RiskMapDTO>> getRiskMap() {
        return RestResult.success(dashboardService.getRiskMap());
    }

    @GetMapping("/graph")
    public RestResult<CompanyGraphDTO> getCompanyKnowledgeGraph() {
        return RestResult.success(dashboardService.getCompanyKnowledgeGraph());
    }
}
