package org.example.riskwarningsystembackend.module_monitoring.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

/**
 * 资讯实体类 (包括新闻和风险预警)
 */
@Data
@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String type; // 'news' 或 'risk'

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(length = 100)
    private String author;

    @Column(length = 50)
    private String date;

    @Column(length = 255)
    private String image;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private List<String> tags;

    // --- 仅风险(risk)类型资讯有的字段 ---
    @Column(columnDefinition = "TEXT")
    private String riskSource;

    @Column(columnDefinition = "TEXT")
    private String notice;

    @Column(length = 255)
    private String relatedCompany;

    @Column(length = 255)
    private String relatedProduct;

    @Column(columnDefinition = "TEXT")
    private String content; // HTML格式的正文
}
