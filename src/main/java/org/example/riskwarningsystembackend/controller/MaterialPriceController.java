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

    @GetMapping("/quotas")
    public RestResult<List<SmmIndicator>> getAllIndicators() {
        return RestResult.success(indicatorRepository.findAll());
    }

    @GetMapping("/prices")
    public RestResult<List<PriceDataDto>> getPrices(
            @RequestParam Long indicatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PriceDataDto> prices = priceDataRepository.findPricesByIndicatorIdAndDateRange(indicatorId, startDate, endDate);
        return RestResult.success(prices);
    }

    @GetMapping("/predict")
    public RestResult<List<PriceDataDto>> getPrediction(
            @RequestParam Long indicatorId,
            @RequestParam(defaultValue = "30") int days) {
        List<PriceDataDto> prediction = pricePredictionService.predictFuturePrices(indicatorId, days);
        return RestResult.success(prediction);
    }

    @PostMapping("/admin/sync-indicators")
    public ResponseEntity<?> syncIndicators() {
        try {
            smmApiService.syncIndicators();
            return ResponseEntity.ok().body("指标列表同步任务已启动。");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("指标同步失败: " + e.getMessage());
        }
    }

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
