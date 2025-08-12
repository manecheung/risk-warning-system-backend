package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@Table(name = "company_relations", uniqueConstraints = {
    // A relationship is unique based on the pair of companies, the product context, and the relation type
    @UniqueConstraint(columnNames = {"company_one_id", "company_two_id", "shared_product_name", "relation_type"})
})
public class CompanyRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键

    @Column(name = "company_one_id", nullable = false)
    private Long companyOneId; // 公司1的ID

    @Column(name = "company_two_id", nullable = false)
    private Long companyTwoId; // 公司2的ID

    @Column(name = "shared_product_name", nullable = false)
    private String sharedProductName; // 共享的产品名称

    @Column(name = "relation_type", nullable = false)
    private String relationType; // 关联类型

    public CompanyRelation(Long companyOneId, Long companyTwoId, String sharedProductName, String relationType) {
        // Ensure companyOneId is always less than companyTwoId to avoid duplicate relations (A,B) vs (B,A)
        if (companyOneId < companyTwoId) {
            this.companyOneId = companyOneId;
            this.companyTwoId = companyTwoId;
        } else {
            this.companyOneId = companyTwoId;
            this.companyTwoId = companyOneId;
        }
        this.sharedProductName = sharedProductName;
        this.relationType = relationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyRelation that = (CompanyRelation) o;
        return Objects.equals(companyOneId, that.companyOneId) &&
               Objects.equals(companyTwoId, that.companyTwoId) &&
               Objects.equals(sharedProductName, that.sharedProductName) &&
               Objects.equals(relationType, that.relationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyOneId, companyTwoId, sharedProductName, relationType);
    }
}
