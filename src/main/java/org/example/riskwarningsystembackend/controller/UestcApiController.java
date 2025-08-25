package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.uestc.*;
import org.example.riskwarningsystembackend.service.UestcApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * {@code @description} 电子科技大学相关API的控制器
 * {@code @date} 2025/08/22
 */
@RestController
@RequestMapping("/api/uestc")
public class UestcApiController {

    private final UestcApiService uestcApiService;

    public UestcApiController(UestcApiService uestcApiService) {
        this.uestcApiService = uestcApiService;
    }

    /**
     * 获取产业链列表
     *
     * @return 包含产业链信息的列表
     */
    @GetMapping("/industry-chain/list")
    public RestResult<List<Map<String, Object>>> getIndustryChainList() {
        return uestcApiService.getIndustryChainList();
    }

    /**
     * 获取指定产业链的风险图谱
     *
     * @param industryChainId 产业链ID
     * @return 风险图谱数据
     */
    @GetMapping("/risk-status/graph/{industryChainId}")
    public RestResult<UestcGraphDTO> getRiskGraph(@PathVariable Integer industryChainId) {
        return uestcApiService.getRiskGraph(industryChainId);
    }

    /**
     * 获取指定产业链的风险状态可用时段
     *
     * @param industryChainId 产业链ID
     * @return 可用时段列表
     */
    @GetMapping("/risk-status/periods/{industryChainId}")
    public RestResult<List<String>> getRiskStatusPeriods(@PathVariable Integer industryChainId) {
        return uestcApiService.getRiskStatusPeriods(industryChainId);
    }

    /**
     * 获取指定产业链和时段的风险状态概览
     *
     * @param industryChainId 产业链ID
     * @param dataPeriod 数据时段
     * @return 风险状态概览信息
     */
    @GetMapping("/risk-status/overview/{industryChainId}")
    public RestResult<RiskStatusOverviewDTO> getRiskStatusOverview(@PathVariable Integer industryChainId, @RequestParam String dataPeriod) {
        return uestcApiService.getRiskStatusOverview(industryChainId, dataPeriod);
    }

    /**
     * 分页获取已训练模型列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页的模型数据
     */
    @GetMapping("/trained-models")
    public RestResult<UestcPageDTO<TrainedModelDTO>> getTrainedModels(@RequestParam(defaultValue = "1") int current, @RequestParam(defaultValue = "10") int size) {
        return uestcApiService.getTrainedModels(current, size);
    }

    /**
     * 获取指定模型的训练图表
     *
     * @param id 模型ID
     * @return 训练图表列表
     */
    @GetMapping("/trained-models/{id}/training-plots")
    public RestResult<List<TrainingPlotDTO>> getTrainingPlots(@PathVariable Integer id) {
        return uestcApiService.getTrainingPlots(id);
    }

    /**
     * 获取训练图表图片
     *
     * @param id 模型ID
     * @param filename 图片文件名
     * @return 图片字节数据的响应实体
     */
    @GetMapping("/trained-models/{id}/training-plots/{filename}")
    public ResponseEntity<byte[]> getTrainingPlotImage(@PathVariable Integer id, @PathVariable String filename) {
        return uestcApiService.getTrainingPlotImage(id, filename);
    }
}
