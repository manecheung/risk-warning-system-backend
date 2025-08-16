package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 产品信息实体类
 * 用于映射数据库中的产品信息表，存储产品的多级分类信息
 */
@Entity
@Data
@Table(name = "product_info")
public class ProductInfo {

    /**
     * 产品信息主键ID
     * 使用数据库自增策略生成唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 一级产品分类
     */
    @Column(name = "level1")
    private String level1; // 一级产品

    /**
     * 二级产品分类
     */
    @Column(name = "level2")
    private String level2; // 二级产品

    /**
     * 三级产品分类
     */
    @Column(name = "level3")
    private String level3; // 三级产品

    /**
     * 四级产品分类
     */
    @Column(name = "level4")
    private String level4; // 四级产品

    /**
     * 五级产品分类
     */
    @Column(name = "level5")
    private String level5; // 五级产品
}
