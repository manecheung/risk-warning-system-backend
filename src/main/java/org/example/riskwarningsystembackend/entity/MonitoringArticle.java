package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

/**
 * 监控文章实体类
 * 用于表示新闻或风险监控文章的信息
 */
@Data
@Entity
@Table(name = "monitoring_articles")
public class MonitoringArticle {

    // Getters and Setters

    /**
     * 文章唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文章类型
     * 可选值: 'news'(新闻) 或 'risk'(风险)
     */
    @Column(nullable = false)
    private String type;

    /**
     * 文章标题
     * 最大长度512个字符
     */
    @Column(nullable = false, length = 512)
    private String title;

    /**
     * 文章作者
     */
    private String author;

    /**
     * 文章发布日期
     */
    @Column(name = "publish_date")
    private LocalDate date;

    /**
     * 文章链接地址
     */
    private String url;

    /**
     * 文章图片链接
     */
    private String image;

    /**
     * 文章标签列表
     * 存储在单独的monitoring_article_tags表中
     */
    @ElementCollection
    @CollectionTable(name = "monitoring_article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private List<String> tags;

    /**
     * 风险来源
     */
    @Column(name = "risk_source")
    private String riskSource;

    /**
     * 公告内容
     * 使用LONGVARCHAR类型存储较长的文本内容
     */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String notice;

    /**
     * 相关公司
     */
    @Column(name = "related_company")
    private String relatedCompany;

    /**
     * 相关产品
     */
    @Column(name = "related_product")
    private String relatedProduct;

    /**
     * 文章正文内容
     * 使用LONGVARCHAR类型存储较长的文本内容
     */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false)
    private String content;

}
