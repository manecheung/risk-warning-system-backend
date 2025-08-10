package org.example.riskwarningsystembackend.module_chain_risk.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.riskwarningsystembackend.module_supply_chain.entity.Company;

/**
 * 公司关系实体（图的边）
 */
@Data
@Entity
@Table(name = "company_relationships")
public class CompanyRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_company_id", nullable = false)
    private Company source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_company_id", nullable = false)
    private Company target;

    @Column(length = 50)
    private String label; // e.g., "供应", "销售", "合作"

    @Column(length = 50)
    private String type; // e.g., "supplier", "customer", "partner"
}
