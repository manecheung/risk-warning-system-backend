package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 公司关系实体类，用于表示两个公司之间在某个共享产品上下文下的特定类型的关系。
 * 该类通过数据库表 "company_relations" 映射，确保公司配对、产品名称、关系名称和关系类型的组合唯一。
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "company_relations", uniqueConstraints = {
        // 一条关系是否唯一，取决于公司配对、产品上下文以及关系类型这三者的组合。
        @UniqueConstraint(columnNames = {"company_one_id", "company_two_id", "shared_product_name", "relation_name", "relation_type"})})
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

    /**
     * 构造函数，创建一个公司关系对象。
     * 在构造时会自动保证 companyOneId 小于 companyTwoId，防止 (A,B) 和 (B,A) 被视为不同关系。
     *
     * @param companyOneId      第一个公司的ID
     * @param companyTwoId      第二个公司的ID
     * @param sharedProductName 共享的产品名称
     * @param relationName      关系名称
     * @param relationType      关系类型
     */
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

    /**
     * 判断两个 CompanyRelation 对象是否相等。
     * 比较依据为：companyOneId、companyTwoId、sharedProductName 和 relationType 是否都相等。
     *
     * @param o 要比较的对象
     * @return 如果两个对象相等则返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyRelation that = (CompanyRelation) o;
        return Objects.equals(companyOneId, that.companyOneId) && Objects.equals(companyTwoId, that.companyTwoId) && Objects.equals(sharedProductName, that.sharedProductName) && Objects.equals(relationType, that.relationType);
    }

    /**
     * 计算当前对象的哈希码。
     * 基于 companyOneId、companyTwoId、sharedProductName 和 relationType 计算。
     *
     * @return 当前对象的哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(companyOneId, companyTwoId, sharedProductName, relationType);
    }
}
