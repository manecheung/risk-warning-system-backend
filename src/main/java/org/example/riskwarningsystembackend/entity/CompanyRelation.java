package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@Table(name = "company_relations", uniqueConstraints = {
        // 一条关系是否唯一，取决于公司配对、产品上下文以及关系类型这三者的组合。
        @UniqueConstraint(columnNames = {"company_one_id", "company_two_id", "shared_product_name", "relation_type"})})
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

    @Column(name = "relation_name", nullable = false)
    private String relationName; // 关联类型名称

    @Column(name = "relation_type", nullable = false)
    private String relationType; // 关联类型

    public CompanyRelation(Long companyOneId, Long companyTwoId, String sharedProductName, String relationName, String relationType) {
        // 确保 companyOneId 始终小于 companyTwoId，以避免出现 (A,B) 与 (B,A) 这样的重复关系。
        if (companyOneId < companyTwoId) {
            this.companyOneId = companyOneId;
            this.companyTwoId = companyTwoId;
        } else {
            this.companyOneId = companyTwoId;
            this.companyTwoId = companyOneId;
        }
        this.sharedProductName = sharedProductName;
        this.relationName = relationName;
        this.relationType = relationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyRelation that = (CompanyRelation) o;
        return Objects.equals(companyOneId, that.companyOneId) && Objects.equals(companyTwoId, that.companyTwoId) && Objects.equals(sharedProductName, that.sharedProductName) && Objects.equals(relationType, that.relationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyOneId, companyTwoId, sharedProductName, relationType);
    }
}
