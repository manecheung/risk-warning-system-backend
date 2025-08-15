package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟过程中的单个步骤
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationStepDTO {
    /**
     * 动画步骤的时间点 (0, 1, 2...)
     */
    private int step;

    /**
     * 状态发生变化的节点ID
     */
    private String nodeId;

    /**
     * 状态发生变化的节点名称
     */
    private String nodeLabel;

    /**
     * 变化后的风险值
     */
    private double riskValue;

    /**
     * 节点所在的蔓延层级
     */
    private int level;

    /**
     * 节点变化后的状态 (如: CRITICAL, WARNING)
     */
    private String status;

    /**
     * 引发本次变化的源头节点ID
     */
    private String triggeredBy;
}