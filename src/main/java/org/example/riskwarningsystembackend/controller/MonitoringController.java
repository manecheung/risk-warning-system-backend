package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDto;
import org.example.riskwarningsystembackend.dto.monitoring.ArticleDetailDto;
import org.example.riskwarningsystembackend.dto.monitoring.ArticleRecordDto;
import org.example.riskwarningsystembackend.service.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    @Autowired
    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/articles")
    public RestResult<PaginatedResponseDto<ArticleRecordDto>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return RestResult.success(monitoringService.getArticles(page, pageSize, type, keyword));
    }

    @GetMapping("/articles/{id}")
    public RestResult<ArticleDetailDto> getArticleById(@PathVariable int id) {
        return monitoringService.getArticleById(id)
                .map(RestResult::success)
                .orElse(RestResult.failure(ResultCode.NOT_FOUND));
    }
}
