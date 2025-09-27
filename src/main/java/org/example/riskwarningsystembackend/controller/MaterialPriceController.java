package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.PriceDataDto;
import org.example.riskwarningsystembackend.entity.SmmIndicator;
import org.example.riskwarningsystembackend.repository.SmmIndicatorRepository;
import org.example.riskwarningsystembackend.repository.SmmPriceDataRepository;
import org.example.riskwarningsystembackend.service.PricePredictionService;
import org.example.riskwarningsystembackend.service.SmmApiService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 材料价格控制器类
 * 处理材料价格相关的REST API请求，包括获取指标、价格数据、价格预测以及同步数据等操作
 */
@RestController
@RequestMapping("/api/materials")
public class MaterialPriceController {

    private final SmmApiService smmApiService;
    private final SmmIndicatorRepository indicatorRepository;
    private final SmmPriceDataRepository priceDataRepository;
    private final PricePredictionService pricePredictionService;

    public MaterialPriceController(SmmApiService smmApiService, SmmIndicatorRepository indicatorRepository, SmmPriceDataRepository priceDataRepository, PricePredictionService pricePredictionService) {
        this.smmApiService = smmApiService;
        this.indicatorRepository = indicatorRepository;
        this.priceDataRepository = priceDataRepository;
        this.pricePredictionService = pricePredictionService;
    }

    /**
     * 获取所有指标列表
     *
     * @return RestResult<List<SmmIndicator>> 包含所有指标的响应结果
     */
    @GetMapping("/quotas")
    public RestResult<List<SmmIndicator>> getAllIndicators() {
        return RestResult.success(indicatorRepository.findAll());
    }

    /**
     * 根据指标ID和日期范围获取价格数据
     *
     * @param indicatorId 指标ID
     * @param startDate 起始日期
     * @param endDate 结束日期
     * @return RestResult<List<PriceDataDto>> 包含价格数据的响应结果
     */
    @GetMapping("/prices")
    public RestResult<List<PriceDataDto>> getPrices(
            @RequestParam Long indicatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PriceDataDto> prices = priceDataRepository.findPricesByIndicatorIdAndDateRange(indicatorId, startDate, endDate);
        return RestResult.success(prices);
    }

    /**
     * 获取指定指标的价格预测数据
     *
     * @param indicatorId 指标ID
     * @param days 预测天数，默认为30天
     * @return RestResult<List<PriceDataDto>> 包含预测价格数据的响应结果
     */
    @GetMapping("/predict")
    public RestResult<List<PriceDataDto>> getPrediction(
            @RequestParam Long indicatorId,
            @RequestParam(defaultValue = "7") int days) {
        List<PriceDataDto> prediction = pricePredictionService.predictFuturePrices(indicatorId, days);
        return RestResult.success(prediction);
    }

    /**
     * 同步所有指标列表（管理员接口）
     *
     * @return ResponseEntity<?> 同步操作结果响应
     */
    @PostMapping("/admin/sync-indicators")
    public ResponseEntity<?> syncIndicators() {
        try {
            smmApiService.syncIndicators();
            return ResponseEntity.ok().body("指标列表同步任务已启动。");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("指标同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步指定指标的价格数据（管理员接口）
     *
     * @param quotaId 指标配额ID
     * @param startDate 起始日期
     * @param endDate 结束日期
     * @return ResponseEntity<?> 同步操作结果响应
     */
    @PostMapping("/admin/sync-prices")
    public ResponseEntity<?> syncPrices(
            @RequestParam String quotaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            smmApiService.syncPriceDataForIndicator(quotaId, startDate, endDate);
            return ResponseEntity.ok().body("指标 " + quotaId + " 的价格数据同步任务已启动。");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("价格同步失败: " + e.getMessage());
        }
    }
}
