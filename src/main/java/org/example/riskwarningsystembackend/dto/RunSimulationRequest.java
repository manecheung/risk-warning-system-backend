package org.example.riskwarningsystembackend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 运行一次新模拟的请求体
 */
@Data
public class RunSimulationRequest {
    /**
     * 起始节点（公司）的名称
     */
    @NotEmpty(message = "startNodeName is required")
    private String startNodeName;

    /**
     * 初始风险值 (可选)
     */
    private Double initialRisk;

    /**
     * 风险衰减率 (可选)
     */
    private Double decayRate;

    /**
     * 最大蔓延层级 (可选)
     */
    private Integer maxLevel;
}