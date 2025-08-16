package org.example.riskwarningsystembackend.entity.CompanySimulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * ProductData类用于表示产品数据实体，映射到数据库中的product_data表。
 * 该类包含了产品的基本信息以及与竞争对手和客户相关的数据。
 */
@Entity
@Table(name = "product_data")
@Data
public class ProductData {
    /**
     * 产品数据的唯一标识符，主键，自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 产品名称，对应JSON中的'Name'字段
     */
    @JsonProperty("Name")
    private String name;

    /**
     * 产品的权重值，对应JSON中的'W'字段
     */
    @JsonProperty("W")
    private Double w;

    /**
     * 产品数量，对应JSON中的'Nums'字段
     */
    @JsonProperty("Nums")
    private Double nums;

    /**
     * 产品的因子值，对应JSON中的'F'字段
     */
    @JsonProperty("F")
    private Double f;

    /**
     * 竞争对手列表，存储竞争对手的ID，对应JSON中的'WithCompetitors'字段
     * 使用ElementCollection注解将集合映射到单独的数据库表product_data_competitors中
     */
    @ElementCollection
    @CollectionTable(name = "product_data_competitors", joinColumns = @JoinColumn(name = "product_data_id"))
    @Column(name = "competitor_id")
    @JsonProperty("WithCompetitors")
    private List<Integer> withCompetitors;

    /**
     * 客户映射关系，键为客户ID，值为对应的数值，对应JSON中的'ToCustomers'字段
     * 使用ElementCollection注解将Map映射到单独的数据库表product_data_customers中
     */
    @ElementCollection
    @CollectionTable(name = "product_data_customers", joinColumns = @JoinColumn(name = "product_data_id"))
    @MapKeyColumn(name = "customer_id")
    @Column(name = "value")
    @JsonProperty("ToCustomers")
    private Map<String, Double> toCustomers;
}
