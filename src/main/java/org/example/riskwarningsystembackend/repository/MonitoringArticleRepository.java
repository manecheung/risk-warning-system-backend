package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.MonitoringArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MonitoringArticleRepository extends JpaRepository<MonitoringArticle, Long>, JpaSpecificationExecutor<MonitoringArticle> {
}