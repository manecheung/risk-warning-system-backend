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
            @RequestParam(defaultValue = "20") int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        return RestResult.success(dashboardService.getRiskAnalysis(pageRequest));
    }

    @GetMapping("/risk-map")
    public RestResult<List<RiskMapDTO>> getRiskMap() {
        return RestResult.success(dashboardService.getRiskMap());
    }

    /**
     * 获取知识图谱数据的API接口。
     * @param companyId 可选参数。如果前端在请求时附带此参数，则触发按需加载，返回指定公司的子图。
     * @param keyword 可选参数。如果前端附带此参数，则触发搜索功能，返回与关键词相关的子图。
     * @return 经过优化的图谱数据（初始、子图或搜索结果）
     */
    @GetMapping("/graph")
    public RestResult<CompanyGraphDTO> getCompanyKnowledgeGraph(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String keyword) {
        return RestResult.success(dashboardService.getCompanyKnowledgeGraph(companyId, keyword));
    }
}