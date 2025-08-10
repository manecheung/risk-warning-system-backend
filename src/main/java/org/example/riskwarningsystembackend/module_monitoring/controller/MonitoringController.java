package org.example.riskwarningsystembackend.module_monitoring.controller;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.common.dto.Result;
import org.example.riskwarningsystembackend.module_monitoring.entity.Article;
import org.example.riskwarningsystembackend.module_monitoring.service.MonitoringService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 风险监测模块控制器
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    /**
     * 4.1 获取监测资讯列表
     */
    @GetMapping("/articles")
    public ResponseEntity<Result<Map<String, Object>>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Article> articlePage = monitoringService.getArticles(pageable, type, keyword);

        Map<String, Object> response = Map.of(
                "page", articlePage.getNumber() + 1,
                "pageSize", articlePage.getSize(),
                "totalRecords", articlePage.getTotalElements(),
                "totalPages", articlePage.getTotalPages(),
                "hasPrevPage", articlePage.hasPrevious(),
                "hasNextPage", articlePage.hasNext(),
                "records", articlePage.getContent()
        );
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 4.2 获取资讯详情
     */
    @GetMapping("/articles/{id}")
    public ResponseEntity<Result<Article>> getArticleById(@PathVariable Long id) {
        return monitoringService.getArticleById(id)
                .map(article -> ResponseEntity.ok(Result.success(article)))
                .orElse(ResponseEntity.notFound().build());
    }
}