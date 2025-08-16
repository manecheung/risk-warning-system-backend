package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品节点实体类
 * 用于表示产品层级结构中的节点信息
 */
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

    /**
     * 构造函数，用于创建指定名称和层级的产品节点
     * @param name 产品名称
     * @param level 产品层级
     */
    public ProductNode(String name, int level) {
        this.name = name;
        this.level = level;
    }
}

