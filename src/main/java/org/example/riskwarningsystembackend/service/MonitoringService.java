package org.example.riskwarningsystembackend.service;

import jakarta.persistence.criteria.Predicate;
import org.example.riskwarningsystembackend.dto.monitoring.ArticleDTO;
import org.example.riskwarningsystembackend.entity.MonitoringArticle;
import org.example.riskwarningsystembackend.exception.ResourceNotFoundException;
import org.example.riskwarningsystembackend.repository.MonitoringArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 监控文章服务类，提供对监控文章的增删改查功能。
 */
@Service
public class MonitoringService {

    private final MonitoringArticleRepository monitoringArticleRepository;

    public MonitoringService(MonitoringArticleRepository monitoringArticleRepository) {
        this.monitoringArticleRepository = monitoringArticleRepository;
    }

    /**
     * 分页查询监控文章列表，支持按类型和关键词（标题或标签）进行筛选。
     *
     * @param page     当前页码（从1开始）
     * @param pageSize 每页记录数
     * @param type     文章类型筛选条件（可选）
     * @param keyword  关键词筛选条件（可选），用于匹配标题或标签
     * @return 分页结果，包含符合条件的文章列表
     */
    public Page<MonitoringArticle> getArticles(int page, int pageSize, String type, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("date").descending());

        Specification<MonitoringArticle> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(type)) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            if (StringUtils.hasText(keyword)) {
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
                // 正确连接并搜索标签集合
                Predicate tagsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.join("tags", jakarta.persistence.criteria.JoinType.LEFT)), "%" + keyword.toLowerCase() + "%");
                predicates.add(criteriaBuilder.or(titlePredicate, tagsPredicate));
            }

            // 使用 distinct 避免因标签连接导致的重复数据
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return monitoringArticleRepository.findAll(spec, pageable);
    }

    /**
     * 根据ID获取监控文章。
     *
     * @param id 文章ID
     * @return 包含文章信息的Optional对象，若未找到则为空
     */
    public Optional<MonitoringArticle> getArticleById(Long id) {
        return monitoringArticleRepository.findById(id);
    }

    /**
     * 创建新的监控文章。
     *
     * @param articleDto 包含文章信息的数据传输对象
     * @return 保存后的监控文章实体
     */
    @Transactional
    public MonitoringArticle createArticle(ArticleDTO articleDto) {
        MonitoringArticle article = new MonitoringArticle();
        article.setType(articleDto.getType());
        article.setTitle(articleDto.getTitle());
        article.setAuthor(articleDto.getAuthor());
        article.setDate(articleDto.getDate());
        article.setImage(articleDto.getImage());
        article.setTags(articleDto.getTags());
        article.setRiskSource(articleDto.getRiskSource());
        article.setNotice(articleDto.getNotice());
        article.setRelatedCompany(articleDto.getRelatedCompany());
        article.setRelatedProduct(articleDto.getRelatedProduct());
        article.setContent(articleDto.getContent());

        return monitoringArticleRepository.save(article);
    }

    /**
     * 更新指定ID的监控文章。
     *
     * @param id         要更新的文章ID
     * @param articleDto 包含更新信息的数据传输对象
     * @return 更新后的监控文章实体
     * @throws ResourceNotFoundException 若未找到指定ID的文章
     */
    @Transactional
    public MonitoringArticle updateArticle(Long id, ArticleDTO articleDto) {
        MonitoringArticle article = monitoringArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));

        article.setType(articleDto.getType());
        article.setTitle(articleDto.getTitle());
        article.setAuthor(articleDto.getAuthor());
        article.setDate(articleDto.getDate());
        article.setImage(articleDto.getImage());
        article.setTags(articleDto.getTags());
        article.setRiskSource(articleDto.getRiskSource());
        article.setNotice(articleDto.getNotice());
        article.setRelatedCompany(articleDto.getRelatedCompany());
        article.setRelatedProduct(articleDto.getRelatedProduct());
        article.setContent(articleDto.getContent());

        return monitoringArticleRepository.save(article);
    }

    /**
     * 删除指定ID的监控文章。
     *
     * @param id 要删除的文章ID
     * @throws ResourceNotFoundException 若未找到指定ID的文章
     */
    @Transactional
    public void deleteArticle(Long id) {
        if (!monitoringArticleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article not found with id: " + id);
        }
        monitoringArticleRepository.deleteById(id);
    }
}
