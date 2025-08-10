package org.example.riskwarningsystembackend.module_monitoring.service;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_monitoring.entity.Article;
import org.example.riskwarningsystembackend.module_monitoring.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 风险监测服务
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringService {

    private final ArticleRepository articleRepository;

    /**
     * 获取资讯列表（分页+筛选）
     * @param pageable 分页参数
     * @param type 资讯类型
     * @param keyword 搜索关键词
     * @return 资讯分页数据
     */
    public Page<Article> getArticles(Pageable pageable, String type, String keyword) {
        // 处理空字符串参数，使其在JPA查询中为null
        String effectiveType = (type != null && type.isEmpty()) ? null : type;
        String effectiveKeyword = (keyword != null && keyword.isEmpty()) ? null : keyword;
        return articleRepository.findByTypeAndKeyword(effectiveType, effectiveKeyword, pageable);
    }

    /**
     * 根据ID获取资讯详情
     * @param id 资讯ID
     * @return Optional<Article>
     */
    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }
}
