package org.example.riskwarningsystembackend.module_supply_chain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 供应链企业实体类
 */
@Getter
@Setter
@ToString(exclude = {"products", "suppliers", "customers", "competitors"})
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // 企业名称

    @Column(length = 20)
    private String tech; // 技术风险

    @Column(length = 20)
    private String finance; // 财务风险

    @Column(length = 20)
    private String law; // 法律风险

    @Column(length = 20)
    private String credit; // 信用风险

    @Column(columnDefinition = "TEXT")
    private String reason; // 风险原因

    private Double longitude; // 经度
    private Double latitude;  // 纬度

    private String registeredCapital; // 注册资本
    private String paidInCapital; // 实缴资本
    private String scale; // 企业规模
    private Integer employeeCount; // 员工人数
    private Integer certificateCount; // 资质证书数量
    private String taxRating; // 税务评级
    private Integer publicSentimentCount; // 舆情数量
    private Integer legalCaseCount; // 法律诉讼数量
    private String industry; // 所属行业
    private Double stockIndex; // 股价/大盘指数
    private Double revenue; // 营收
    private Double assets; // 资产
    private Double profit; // 利润
    @Column(columnDefinition = "TEXT")
    private String registeredAddress; // 注册地

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "company_products",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return id != null && Objects.equals(id, company.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
