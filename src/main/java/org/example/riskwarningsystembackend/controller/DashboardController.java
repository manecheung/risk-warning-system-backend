package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDto;
import org.example.riskwarningsystembackend.dto.dashboard.*;
import org.example.riskwarningsystembackend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/key-metrics")
    public RestResult<List<KeyMetricDto>> getKeyMetrics() {
        return RestResult.success(dashboardService.getKeyMetrics());
    }

    @GetMapping("/risk-distribution")
    public RestResult<List<RiskDistributionDto>> getRiskDistribution() {
        return RestResult.success(dashboardService.getRiskDistribution());
    }

    @GetMapping("/industry-health")
    public RestResult<IndustryHealthDto> getIndustryHealth() {
        return RestResult.success(dashboardService.getIndustryHealth());
    }

    @GetMapping("/supply-chain-risk")
    public RestResult<SupplyChainRiskDto> getSupplyChainRisk() {
        return RestResult.success(dashboardService.getSupplyChainRisk());
    }

    @GetMapping("/risk-analysis")
    public RestResult<PaginatedResponseDto<RiskAnalysisRecordDto>> getRiskAnalysis(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return RestResult.success(dashboardService.getRiskAnalysis(page, pageSize));
    }

    @GetMapping("/risk-map")
    public RestResult<List<RiskMapDto>> getRiskMap() {
        return RestResult.success(dashboardService.getRiskMap());
    }
}
