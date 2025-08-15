package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风险图中“节点”的数据结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskNodeDTO {
    /**
     * 节点ID（通常是公司ID）
     */
    private String id;

    /**
     * 节点标签（通常是公司名称）
     */
    private String label;

    /**
     * 该节点在此次模拟中的风险值
     */
    private double riskValue;

    /**
     * 风险蔓延的层级
     */
    private int level;
}