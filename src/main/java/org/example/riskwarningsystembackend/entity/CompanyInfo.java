package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "company_info")
public class CompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 512)
    private String name;

    @Column(name = "major_product1", length = 512)
    private String majorProduct1;

    @Column(name = "major_product2", length = 512)
    private String majorProduct2;

    @Column(name = "company_type")
    private String companyType;

    @Column(name = "is_diversified")
    private String isDiversified;

    @Column(name = "is_well_known")
    private String isWellKnown;

    @Column(name = "main_products_summary", length = 1024)
    private String mainProductsSummary;

    @Column(name = "related_products", length = 1024)
    private String relatedProducts;

    @Column(name = "registered_capital")
    private String registeredCapital;

    @Column(name = "paid_in_capital")
    private String paidInCapital;

    @Column(name = "company_size")
    private String companySize;

    @Column(name = "employee_count")
    private String employeeCount;

    @Column(name = "qualification_certificate_count")
    private Integer qualificationCertificateCount;

    @Column(name = "tax_rating")
    private String taxRating;

    @Column(name = "public_opinion_count")
    private Integer publicOpinionCount;

    @Column(name = "legal_dispute_count")
    private Integer legalDisputeCount;

    @Column(name = "industry")
    private String industry;

    @Column(name = "stock_price_index")
    private String stockPriceIndex;

    @Column(name = "revenue")
    private String revenue;

    @Column(name = "assets")
    private String assets;

    @Column(name = "profit")
    private String profit;

    @Column(name = "registered_address", length = 512)
    private String registeredAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}
