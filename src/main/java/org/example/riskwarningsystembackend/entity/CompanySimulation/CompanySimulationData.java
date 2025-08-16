package org.example.riskwarningsystembackend.entity.CompanySimulation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.example.riskwarningsystembackend.entity.Simulation;

import java.util.List;

/**
 * 公司模拟数据实体类
 * 用于存储公司在模拟环境中的各种数据和状态信息
 */
@Entity
@Table(name = "company_simulation_data")
@Data
public class CompanySimulationData {
    /**
     * 主键ID，自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    /**
     * 关联的模拟场景
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id")
    @JsonIgnore
    private Simulation simulation;

    /**
     * 公司ID，用于标识具体的公司
     */
    @JsonProperty("ID")
    private Integer companyId;

    /**
     * 公司名称
     */
    @JsonProperty("Name")
    private String name;

    /**
     * 时间戳，表示数据对应的时间点
     */
    @JsonProperty("Time")
    private Integer time;

    /**
     * 状态标识，表示公司当前的模拟状态
     */
    @JsonProperty("State")
    private Integer state;

    /**
     * 公司模拟列表X数据，存储一组双精度浮点数值
     */
    @ElementCollection
    @CollectionTable(name = "company_simulation_list_x", joinColumns = @JoinColumn(name = "simulation_id"))
    @Column(name = "value")
    @JsonProperty("ListX")
    private List<Double> listX;

    /**
     * 内部因子，用于计算公司内部影响因素
     */
    @JsonProperty("InnerFactor")
    private Double innerFactor;

    /**
     * 产品数据列表，存储公司所有产品的模拟数据
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "simulation_id")
    @JsonProperty("Products")
    private List<ProductData> products;

    /**
     * 竞争因子，用于计算市场竞争影响因素
     */
    @JsonProperty("CompeteFactor")
    private Double competeFactor;

    /**
     * 原材料数据列表，存储公司所有原材料的模拟数据
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "simulation_id")
    @JsonProperty("Materials")
    private List<MaterialData> materials;

    /**
     * 原材料因子，用于计算原材料影响因素
     */
    @JsonProperty("MaterialFactor")
    private Double materialFactor;

    /**
     * 关键风险指标(KRI)数据，存储公司的风险评估信息
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "kri_id")
    @JsonProperty("KRIs")
    private KRI kris;
}
