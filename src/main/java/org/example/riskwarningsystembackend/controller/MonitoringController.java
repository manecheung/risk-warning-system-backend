package org.example.riskwarningsystembackend.controller;

import jakarta.validation.Valid;
import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.ArticleDTO;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDTO;
import org.example.riskwarningsystembackend.entity.MonitoringArticle;
import org.example.riskwarningsystembackend.service.MonitoringService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/monitoring/articles")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping
    public RestResult<PaginatedResponseDTO<MonitoringArticle>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {

        Page<MonitoringArticle> articlePage = monitoringService.getArticles(page, pageSize, type, keyword);
        return RestResult.success(new PaginatedResponseDTO<>(articlePage));
    }

    @GetMapping("/{id}")
    public RestResult<MonitoringArticle> getArticleById(@PathVariable Long id) {
        Optional<MonitoringArticle> article = monitoringService.getArticleById(id);
        return article.map(RestResult::success)
                .orElse(RestResult.failure(ResultCode.NOT_FOUND, "Article not found"));
    }

    @PostMapping
    public ResponseEntity<RestResult<MonitoringArticle>> createArticle(@Valid @RequestBody ArticleDTO articleDto) {
        MonitoringArticle createdArticle = monitoringService.createArticle(articleDto);
        return new ResponseEntity<>(RestResult.success(createdArticle), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public RestResult<MonitoringArticle> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleDTO articleDto) {
        MonitoringArticle updatedArticle = monitoringService.updateArticle(id, articleDto);
        return RestResult.success(updatedArticle);
    }

    @DeleteMapping("/{id}")
    public RestResult<Void> deleteArticle(@PathVariable Long id) {
        monitoringService.deleteArticle(id);
        return RestResult.success(null);
    }
}