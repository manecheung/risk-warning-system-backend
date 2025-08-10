package org.example.riskwarningsystembackend.module_chain_risk.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 风险蔓延模拟场景实体
 */
@Data
@Entity
@Table(name = "simulations")
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String creator;

    @Column(columnDefinition = "TIMESTAMP")
    private java.time.LocalDateTime createTime;

    // 将图数据序列化为JSON字符串存储
    @Column(columnDefinition = "TEXT")
    private String nodesJson;

    @Column(columnDefinition = "TEXT")
    private String edgesJson;

    @Column(columnDefinition = "TEXT")
    private String riskPathJson;
}
