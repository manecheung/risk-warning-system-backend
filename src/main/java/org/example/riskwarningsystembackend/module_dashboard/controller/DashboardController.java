package org.example.riskwarningsystembackend.module_dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.common.dto.Result;
import org.example.riskwarningsystembackend.module_dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 首页仪表盘模块控制器
 * 提供首页展示所需的各类数据接口。
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 3.1 获取关键指标
     */
    @GetMapping("/key-metrics")
    public ResponseEntity<Result<List<Map<String, Object>>>> getKeyMetrics() {
        return ResponseEntity.ok(Result.success(dashboardService.getKeyMetrics()));
    }

    /**
     * 3.2 获取风险企业分布 (饼图)
     */
    @GetMapping("/risk-distribution")
    public ResponseEntity<Result<List<Map<String, Object>>>> getRiskDistribution() {
        return ResponseEntity.ok(Result.success(dashboardService.getRiskDistribution()));
    }

    /**
     * 3.3 获取行业健康度排行 (条形图)
     */
    @GetMapping("/industry-health")
    public ResponseEntity<Result<Map<String, Object>>> getIndustryHealth() {
        return ResponseEntity.ok(Result.success(dashboardService.getIndustryHealth()));
    }

    /**
     * 3.4 获取产业链风险分析 (雷达图)
     */
    @GetMapping("/supply-chain-risk")
    public ResponseEntity<Result<Map<String, Object>>> getSupplyChainRisk() {
        return ResponseEntity.ok(Result.success(dashboardService.getSupplyChainRisk()));
    }

    /**
     * 3.5 获取最新风险分析列表
     */
    @GetMapping("/risk-analysis")
    public ResponseEntity<Result<Map<String, Object>>> getRiskAnalysis(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ResponseEntity.ok(Result.success(dashboardService.getRiskAnalysis(page, pageSize)));
    }

    /**
     * 3.6 获取风险企业地图分布
     */
    @GetMapping("/risk-map")
    public ResponseEntity<Result<List<Map<String, Object>>>> getRiskMap() {
        return ResponseEntity.ok(Result.success(dashboardService.getRiskMap()));
    }
}
