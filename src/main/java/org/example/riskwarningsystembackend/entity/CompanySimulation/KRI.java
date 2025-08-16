package org.example.riskwarningsystembackend.entity.CompanySimulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

/**
 * KRI实体类，用于表示关键风险指标（Key Risk Indicators）数据。
 * 该类映射到数据库表"kri"，包含多个KCI（Key Control Indicator）嵌入对象以及相关评分字段。
 */
@Entity
@Table(name = "kri")
@Data
public class KRI {
    /**
     * 主键ID，自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * KZ指标值，对应JSON中的'KZ'
     */
    @JsonProperty("KZ")
    private Double kz;

    /**
     * KC1指标对象，包含k值、w值列表和score值，对应JSON中的'KC1'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc1_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc1_score"))
    })
    @JsonProperty("KC1")
    private KCI kc1;

    @ElementCollection
    @CollectionTable(name = "kri_kc1_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc1W;

    /**
     * KC2指标对象，包含k值、w值列表和score值，对应JSON中的'KC2'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc2_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc2_score"))
    })
    @JsonProperty("KC2")
    private KCI kc2;

    @ElementCollection
    @CollectionTable(name = "kri_kc2_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc2W;

    /**
     * KC3指标对象，包含k值、w值列表和score值，对应JSON中的'KC3'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc3_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc3_score"))
    })
    @JsonProperty("KC3")
    private KCI kc3;

    @ElementCollection
    @CollectionTable(name = "kri_kc3_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc3W;

    /**
     * KC4指标对象，包含k值、w值列表和score值，对应JSON中的'KC4'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc4_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc4_score"))
    })
    @JsonProperty("KC4")
    private KCI kc4;

    @ElementCollection
    @CollectionTable(name = "kri_kc4_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc4W;

    /**
     * KC5指标对象，包含k值、w值列表和score值，对应JSON中的'KC5'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc5_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc5_score"))
    })
    @JsonProperty("KC5")
    private KCI kc5;

    @ElementCollection
    @CollectionTable(name = "kri_kc5_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc5W;

    /**
     * KC6指标对象，包含k值、w值列表和score值，对应JSON中的'KC6'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc6_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc6_score"))
    })
    @JsonProperty("KC6")
    private KCI kc6;

    @ElementCollection
    @CollectionTable(name = "kri_kc6_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc6W;

    /**
     * KC7指标对象，包含k值、w值列表和score值，对应JSON中的'KC7'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc7_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc7_score"))
    })
    @JsonProperty("KC7")
    private KCI kc7;

    @ElementCollection
    @CollectionTable(name = "kri_kc7_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc7W;

    /**
     * KC8指标对象，包含k值、w值列表和score值，对应JSON中的'KC8'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc8_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc8_score"))
    })
    @JsonProperty("KC8")
    private KCI kc8;

    @ElementCollection
    @CollectionTable(name = "kri_kc8_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc8W;

    /**
     * KC9指标对象，包含k值、w值列表和score值，对应JSON中的'KC9'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc9_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc9_score"))
    })
    @JsonProperty("KC9")
    private KCI kc9;

    @ElementCollection
    @CollectionTable(name = "kri_kc9_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc9W;

    /**
     * KC10指标对象，包含k值、w值列表和score值，对应JSON中的'KC10'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc10_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc10_score"))
    })
    @JsonProperty("KC10")
    private KCI kc10;

    @ElementCollection
    @CollectionTable(name = "kri_kc10_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc10W;

    /**
     * KC11指标对象，包含k值、w值列表和score值，对应JSON中的'KC11'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc11_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc11_score"))
    })
    @JsonProperty("KC11")
    private KCI kc11;

    @ElementCollection
    @CollectionTable(name = "kri_kc11_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc11W;

    /**
     * KF指标值，对应JSON中的'KF'
     */
    @JsonProperty("KF")
    private Double kf;

    /**
     * KC12指标对象，包含k值、w值列表和score值，对应JSON中的'KC12'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc12_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc12_score"))
    })
    @JsonProperty("KC12")
    private KCI kc12;

    @ElementCollection
    @CollectionTable(name = "kri_kc12_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc12W;

    /**
     * KC13指标对象，包含k值、w值列表和score值，对应JSON中的'KC13'
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "k", column = @Column(name = "kc13_k")),
        @AttributeOverride(name = "score", column = @Column(name = "kc13_score"))
    })
    @JsonProperty("KC13")
    private KCI kc13;

    @ElementCollection
    @CollectionTable(name = "kri_kc13_w", joinColumns = @JoinColumn(name = "kri_id"))
    @Column(name = "value")
    private java.util.List<Double> kc13W;

    /**
     * KRIScore总评分值，对应JSON中的'KRIScore'
     */
    @JsonProperty("KRIScore")
    private Double kriScore;
}
