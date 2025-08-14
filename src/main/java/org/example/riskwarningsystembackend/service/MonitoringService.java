package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.ArticleDto;
import org.example.riskwarningsystembackend.entity.MonitoringArticle;
import org.example.riskwarningsystembackend.exception.ResourceNotFoundException;
import org.example.riskwarningsystembackend.repository.MonitoringArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MonitoringService {

    @Autowired
    private MonitoringArticleRepository monitoringArticleRepository;

    public Page<MonitoringArticle> getArticles(int page, int pageSize, String type, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Specification<MonitoringArticle> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(type)) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            if (StringUtils.hasText(keyword)) {
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
                // Correctly join and search in the tags collection
                Predicate tagsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.join("tags", jakarta.persistence.criteria.JoinType.LEFT)), "%" + keyword.toLowerCase() + "%");
                predicates.add(criteriaBuilder.or(titlePredicate, tagsPredicate));
            }

            // Use distinct to avoid duplicates from the tags join
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return monitoringArticleRepository.findAll(spec, pageable);
    }

    public Optional<MonitoringArticle> getArticleById(Long id) {
        return monitoringArticleRepository.findById(id);
    }

    @Transactional
    public MonitoringArticle createArticle(ArticleDto articleDto) {
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

    @Transactional
    public MonitoringArticle updateArticle(Long id, ArticleDto articleDto) {
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

    @Transactional
    public void deleteArticle(Long id) {
        if (!monitoringArticleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article not found with id: " + id);
        }
        monitoringArticleRepository.deleteById(id);
    }
}