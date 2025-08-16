package org.example.riskwarningsystembackend.entity.CompanySimulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Data;

/**
 * KCI实体类，用于表示关键控制指标
 */
@Embeddable
@Data
public class KCI {
    /**
     * K值，表示关键控制指标的核心参数
     */
    @JsonProperty("K")
    private Double k;

    /**
     * W值列表，表示关键控制指标的权重集合
     * 注意：此字段由Jackson在反序列化时填充，但不会被JPA持久化。
     * 数据通过SimulationDataImportService手动传输到KRI实体的相应集合中。
     */
    @Transient
    @JsonProperty("W")
    private java.util.List<Double> w;

    /**
     * 分数值，表示关键控制指标的计算得分
     */
    @JsonProperty("Score")
    private Double score;
}
