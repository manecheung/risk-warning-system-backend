package org.example.riskwarningsystembackend.module_monitoring.repository;

import org.example.riskwarningsystembackend.module_monitoring.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 资讯数据访问接口
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    /**
     * 根据类型、关键词进行分页查询
     * @param type 资讯类型 (news/risk)
     * @param keyword 关键词 (在标题或标签中模糊查询)
     * @param pageable 分页参数
     * @return 资讯分页数据
     */
    @Query("SELECT DISTINCT a FROM Article a LEFT JOIN a.tags t WHERE (:type IS NULL OR a.type = :type) AND (:keyword IS NULL OR a.title LIKE %:keyword% OR t LIKE %:keyword%)")
    Page<Article> findByTypeAndKeyword(@Param("type") String type, @Param("keyword") String keyword, Pageable pageable);

}
