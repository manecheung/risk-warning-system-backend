package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "product_hierarchy")
public class ProductHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(name = "product_name", nullable = false, unique = true)
    private String productName; // 产品名称

    @Column(name = "parent_name")
    private String parentName; // 父级产品名称

    @Column(name = "level")
    private int level; // 层级

    public ProductHierarchy(String productName, String parentName, int level) {
        this.productName = productName;
        this.parentName = parentName;
        this.level = level;
    }
}
