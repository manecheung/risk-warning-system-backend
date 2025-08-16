package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.*;
import org.example.riskwarningsystembackend.dto.dashboard.RiskMapDTO;
import org.example.riskwarningsystembackend.dto.dashboard.*;
import org.example.riskwarningsystembackend.dto.supplychain.SupplyChainRiskDTO;
import org.example.riskwarningsystembackend.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘控制器，提供各类风险预警相关的数据接口。
 * 包括关键指标、风险分布、行业健康度、供应链风险、风险分析列表、风险地图和知识图谱等数据的获取。
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 构造函数注入 DashboardService 实例。
     *
     * @param dashboardService 提供仪表盘相关业务逻辑的服务类
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取关键指标数据。
     *
     * @return 返回封装了关键指标数据的 RestResult 对象
     */
    @GetMapping("/key-metrics")
    public RestResult<List<KeyMetricDTO>> getKeyMetrics() {
        return RestResult.success(dashboardService.getKeyMetrics());
    }

    /**
     * 获取风险分布数据。
     *
     * @return 返回封装了风险分布数据的 RestResult 对象
     */
    @GetMapping("/risk-distribution")
    public RestResult<List<RiskDistributionDTO>> getRiskDistribution() {
        return RestResult.success(dashboardService.getRiskDistribution());
    }

    /**
     * 获取行业健康度数据。
     *
     * @return 返回封装了行业健康度数据的 RestResult 对象
     */
    @GetMapping("/industry-health")
    public RestResult<IndustryHealthDTO> getIndustryHealth() {
        return RestResult.success(dashboardService.getIndustryHealth());
    }

    /**
     * 获取供应链风险数据。
     *
     * @return 返回封装了供应链风险数据的 RestResult 对象
     */
    @GetMapping("/supply-chain-risk")
    public RestResult<SupplyChainRiskDTO> getSupplyChainRisk() {
        return RestResult.success(dashboardService.getSupplyChainRisk());
    }

    /**
     * 分页获取风险分析数据。
     *
     * @param page     当前页码，默认值为 1
     * @param pageSize 每页大小，默认值为 20
     * @return 返回封装了分页风险分析数据的 RestResult 对象
     */
    @GetMapping("/risk-analysis")
    public RestResult<PaginatedResponseDTO<RiskAnalysisDTO>> getRiskAnalysis(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        return RestResult.success(dashboardService.getRiskAnalysis(pageRequest));
    }

    /**
     * 获取风险地图数据。
     *
     * @return 返回封装了风险地图数据的 RestResult 对象
     */
    @GetMapping("/risk-map")
    public RestResult<List<RiskMapDTO>> getRiskMap() {
        return RestResult.success(dashboardService.getRiskMap());
    }

    /**
     * 获取知识图谱数据的API接口。
     *
     * @param companyId 可选参数。如果前端在请求时附带此参数，则触发按需加载，返回指定公司的子图。
     * @param keyword   可选参数。如果前端附带此参数，则触发搜索功能，返回与关键词相关的子图。
     * @return 经过优化的图谱数据（初始、子图或搜索结果）
     */
    @GetMapping("/graph")
    public RestResult<CompanyGraphDTO> getCompanyKnowledgeGraph(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String keyword) {
        return RestResult.success(dashboardService.getCompanyKnowledgeGraph(companyId, keyword));
    }
}
