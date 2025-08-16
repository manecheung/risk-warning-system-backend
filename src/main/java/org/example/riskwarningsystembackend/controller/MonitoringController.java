package org.example.riskwarningsystembackend.controller;

import jakarta.validation.Valid;
import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.monitoring.ArticleDTO;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDTO;
import org.example.riskwarningsystembackend.entity.MonitoringArticle;
import org.example.riskwarningsystembackend.service.MonitoringService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 监控文章管理控制器，提供对监控文章的增删改查接口。
 */
@RestController
@RequestMapping("/api/monitoring/articles")
public class MonitoringController {

    private final MonitoringService monitoringService;

    /**
     * 构造方法注入 MonitoringService 服务类。
     *
     * @param monitoringService 监控服务类实例
     */
    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * 分页获取监控文章列表，支持按类型和关键词进行筛选。
     *
     * @param page     当前页码，默认为 1
     * @param pageSize 每页大小，默认为 10
     * @param type     文章类型（可选）
     * @param keyword  搜索关键词（可选）
     * @return 包含分页数据的统一响应结果
     */
    @GetMapping
    public RestResult<PaginatedResponseDTO<MonitoringArticle>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {

        Page<MonitoringArticle> articlePage = monitoringService.getArticles(page, pageSize, type, keyword);
        return RestResult.success(new PaginatedResponseDTO<>(articlePage));
    }

    /**
     * 根据 ID 获取单个监控文章信息。
     *
     * @param id 文章唯一标识符
     * @return 若找到则返回文章信息，否则返回失败结果
     */
    @GetMapping("/{id}")
    public RestResult<MonitoringArticle> getArticleById(@PathVariable Long id) {
        Optional<MonitoringArticle> article = monitoringService.getArticleById(id);
        // 使用 Optional 判断是否存在，存在则返回成功结果，否则返回未找到错误
        return article.map(RestResult::success)
                .orElse(RestResult.failure(ResultCode.NOT_FOUND, "Article not found"));
    }

    /**
     * 创建新的监控文章。
     *
     * @param articleDto 文章传输对象，包含创建所需的数据
     * @return 创建成功的文章信息及 HTTP 状态码 201
     */
    @PostMapping
    public ResponseEntity<RestResult<MonitoringArticle>> createArticle(@Valid @RequestBody ArticleDTO articleDto) {
        MonitoringArticle createdArticle = monitoringService.createArticle(articleDto);
        return new ResponseEntity<>(RestResult.success(createdArticle), HttpStatus.CREATED);
    }

    /**
     * 更新指定 ID 的监控文章信息。
     *
     * @param id         要更新的文章 ID
     * @param articleDto 更新后的文章数据传输对象
     * @return 更新后的文章信息
     */
    @PutMapping("/{id}")
    public RestResult<MonitoringArticle> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleDTO articleDto) {
        MonitoringArticle updatedArticle = monitoringService.updateArticle(id, articleDto);
        return RestResult.success(updatedArticle);
    }

    /**
     * 删除指定 ID 的监控文章。
     *
     * @param id 要删除的文章 ID
     * @return 删除操作成功的结果
     */
    @DeleteMapping("/{id}")
    public RestResult<Void> deleteArticle(@PathVariable Long id) {
        monitoringService.deleteArticle(id);
        return RestResult.success(null);
    }
}
