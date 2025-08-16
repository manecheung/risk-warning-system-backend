package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品边实体类，用于表示产品之间的父子关系
 * 该实体映射到product_edges表，存储产品节点之间的连接关系
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "product_edges")
public class ProductEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键

    @Column(name = "parent_id", nullable = false)
    private Long parentId; // 父节点ID

    @Column(name = "child_id", nullable = false)
    private Long childId; // 子节点ID

    /**
     * 构造函数，创建产品边对象
     * @param parentId 父节点ID，不能为空
     * @param childId 子节点ID，不能为空
     */
    public ProductEdge(Long parentId, Long childId) {
        this.parentId = parentId;
        this.childId = childId;
    }
}

