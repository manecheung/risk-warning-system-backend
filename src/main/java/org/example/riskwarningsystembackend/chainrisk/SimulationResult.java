package org.example.riskwarningsystembackend.chainrisk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 产业链风险模拟结果类
 * 包含模拟过程的所有步骤记录和最终异常原因
 */
@Data
@AllArgsConstructor
public class SimulationResult {
    private List<SimulationStep> log;
    private Map<String, String> abnormalReason;
}
