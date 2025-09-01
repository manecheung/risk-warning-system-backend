package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.MonitoringArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface MonitoringArticleRepository extends JpaRepository<MonitoringArticle, Long>, JpaSpecificationExecutor<MonitoringArticle> {
    /**
     * 根据URL检查文章是否存在。
     * @param url 文章链接
     * @return 如果存在则返回true，否则返回false
     */
    boolean existsByUrl(String url);
}