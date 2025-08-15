package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风险图中“边”的数据结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskEdgeDTO {
    /**
     * 起始节点ID
     */
    private String source;

    /**
     * 目标节点ID
     */
    private String target;

    /**
     * 边的标签（如：合作）
     */
    private String label;
}