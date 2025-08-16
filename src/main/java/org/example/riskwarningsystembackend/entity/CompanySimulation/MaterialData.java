package org.example.riskwarningsystembackend.entity.CompanySimulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * MaterialData实体类，用于表示材料数据信息
 * 该类映射到数据库中的material_data表，存储材料的基本属性和供应商信息
 */
@Entity
@Table(name = "material_data")
@Data
public class MaterialData {
    /**
     * 材料数据的唯一标识符
     * 使用数据库自动生成的主键策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 材料名称
     * 对应JSON数据中的'Name'字段
     */
    @JsonProperty("Name")
    private String name;

    /**
     * 材料的权重值
     * 对应JSON数据中的'W'字段
     */
    @JsonProperty("W")
    private Double w;

    /**
     * 材料的最大数量限制
     * 对应JSON数据中的'NMax'字段
     */
    @JsonProperty("NMax")
    private Double nMax;

    /**
     * 材料的供应商列表
     * 使用ElementCollection注解将集合映射到单独的表中
     * 对应JSON数据中的'WithSupplier'字段
     */
    @ElementCollection
    @CollectionTable(name = "material_data_suppliers", joinColumns = @JoinColumn(name = "material_data_id"))
    @Column(name = "supplier_id")
    @JsonProperty("WithSupplier")
    private List<Integer> withSupplier;
}
