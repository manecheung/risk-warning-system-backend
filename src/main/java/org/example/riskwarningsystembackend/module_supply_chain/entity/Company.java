package org.example.riskwarningsystembackend.module_supply_chain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * 供应链企业实体类
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 企业名称

    @Column(length = 100)
    private String industry; // 所属行业

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return id != null && Objects.equals(id, company.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
