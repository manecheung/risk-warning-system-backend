package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.MonitoringArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface MonitoringArticleRepository extends JpaRepository<MonitoringArticle, Long>, JpaSpecificationExecutor<MonitoringArticle> {
    /**
     * 根据标题和发布日期查找文章，用于防止重复插入。
     * @param title 文章标题
     * @param date 发布日期
     * @return 可选的文章实体
     */
    Optional<MonitoringArticle> findByTitleAndDate(String title, LocalDate date);
}