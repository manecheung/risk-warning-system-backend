package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "company_info")
public class CompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 512)
    private String name;

    // Product Info
    @Column(name = "major_product1", length = 512)
    private String majorProduct1;

    @Column(name = "major_product2", length = 512)
    private String majorProduct2;

    @Column(name = "main_products_summary", columnDefinition = "TEXT")
    private String mainProductsSummary;

    @Column(name = "related_products", columnDefinition = "TEXT")
    private String relatedProducts;

    // Basic Classification
    @Column(name = "industry", length = 512)
    private String industry;

    @Column(name = "company_type", length = 255)
    private String companyType;

    @Column(name = "is_diversified", length = 255)
    private String isDiversified;

    @Column(name = "is_well_known", length = 255)
    private String isWellKnown;

    // Scale Info
    @Column(name = "company_size", length = 255)
    private String companySize;

    @Column(name = "employee_count", length = 255)
    private String employeeCount;

    // Financial Data
    @Column(name = "registered_capital", length = 255)
    private String registeredCapital;

    @Column(name = "paid_in_capital", length = 255)
    private String paidInCapital;

    @Column(name = "revenue", length = 255)
    private String revenue;

    @Column(name = "assets", length = 255)
    private String assets;

    @Column(name = "profit", length = 255)
    private String profit;

    @Column(name = "stock_price_index", length = 255)
    private String stockPriceIndex;

    // Risk & Rating
    @Column(name = "qualification_certificate_count")
    private Integer qualificationCertificateCount;

    @Column(name = "tax_rating", length = 255)
    private String taxRating;

    @Column(name = "public_opinion_count")
    private Integer publicOpinionCount;

    @Column(name = "legal_dispute_count")
    private Integer legalDisputeCount;

    // Location
    @Column(name = "registered_address", length = 512)
    private String registeredAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}
