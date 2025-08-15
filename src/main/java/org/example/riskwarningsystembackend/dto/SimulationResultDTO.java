package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 一次完整模拟运算的结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResultDTO {
    /**
     * 本次模拟运行的临时唯一ID
     */
    private String runId;

    /**
     * 本次模拟的所有分步过程
     */
    private List<SimulationStepDTO> steps;

    /**
     * 本次模拟最终影响的所有节点状态
     */
    private List<RiskNodeDTO> finalNodes;

    /**
     * 本次模拟最终影响的所有边
     */
    private List<RiskEdgeDTO> finalEdges;
}