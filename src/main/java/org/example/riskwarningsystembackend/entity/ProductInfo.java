package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "product_info")
public class ProductInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level1")
    private String level1; // 一级产品

    @Column(name = "level2")
    private String level2; // 二级产品

    @Column(name = "level3")
    private String level3; // 三级产品

    @Column(name = "level4")
    private String level4; // 四级产品

    @Column(name = "level5")
    private String level5; // 五级产品
}
