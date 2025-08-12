package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "product_nodes")
public class ProductNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(name = "name", nullable = false, unique = true)
    private String name; // 产品名称

    @Column(name = "level", nullable = false)
    private int level; // 产品层级

    public ProductNode(String name, int level) {
        this.name = name;
        this.level = level;
    }
}
