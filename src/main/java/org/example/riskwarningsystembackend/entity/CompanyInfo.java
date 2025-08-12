package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "company_info")
public class CompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(name = "name", length = 512)
    private String name; // 公司名称

    // Product Info
    @Column(name = "major_product1", length = 512)
    private String majorProduct1; // 主要产品1

    @Column(name = "major_product2", length = 512)
    private String majorProduct2; // 主要产品2

    @Column(name = "main_products_summary", columnDefinition = "TEXT")
    private String mainProductsSummary; // 主营产品（公司主要的经营的与风电相关的产品总结归纳）

    @Column(name = "related_products", columnDefinition = "TEXT")
    private String relatedProducts; // 相关产品

    // Basic Classification
    @Column(name = "industry", length = 512)
    private String industry; // 行业

    @Column(name = "company_type")
    private String companyType; // 公司类型

    @Column(name = "is_diversified")
    private String isDiversified; // 企业是否不仅从事风电还跨多个垂直行业？

    @Column(name = "is_well_known")
    private String isWellKnown; // 是否well known

    // Scale Info
    @Column(name = "company_size")
    private String companySize; // 公司规模

    @Column(name = "employee_count")
    private String employeeCount; // 员工数量

    // Financial Data
    @Column(name = "registered_capital")
    private String registeredCapital; // 注册资金

    @Column(name = "paid_in_capital")
    private String paidInCapital; // 实缴资本

    @Column(name = "revenue")
    private String revenue; // 营业额

    @Column(name = "assets")
    private String assets; // 资产

    @Column(name = "profit")
    private String profit; // 利润

    @Column(name = "stock_price_index")
    private String stockPriceIndex; // 股价指数

    // Risk & Rating
    @Column(name = "qualification_certificate_count")
    private Integer qualificationCertificateCount; // 资质证件数量

    @Column(name = "tax_rating")
    private String taxRating; // 税评级

    @Column(name = "public_opinion_count")
    private Integer publicOpinionCount; // 公告数量

    @Column(name = "legal_dispute_count")
    private Integer legalDisputeCount;// 法务纠纷数量

    // Location
    @Column(name = "registered_address", length = 512)
    private String registeredAddress;// 注册地址

    @Column(name = "latitude")
    private Double latitude; // 纬度

    @Column(name = "longitude")
    private Double longitude; // 经度
}
