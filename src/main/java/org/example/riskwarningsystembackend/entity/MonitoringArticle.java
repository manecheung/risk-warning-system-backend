package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "monitoring_articles")
public class MonitoringArticle {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // 'news' or 'risk'

    @Column(nullable = false, length = 512)
    private String title;

    private String author;

    @Column(name = "publish_date")
    private LocalDate date;

    private String image;

    @ElementCollection
    @CollectionTable(name = "monitoring_article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "risk_source")
    private String riskSource;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String notice;

    @Column(name = "related_company")
    private String relatedCompany;

    @Column(name = "related_product")
    private String relatedProduct;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false)
    private String content;

}