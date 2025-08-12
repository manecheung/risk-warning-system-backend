package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public ProductEdge(Long parentId, Long childId) {
        this.parentId = parentId;
        this.childId = childId;
    }
}
