package org.example.riskwarningsystembackend.chainrisk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 产业链风险模拟步骤类
 * 表示模拟过程中的单个步骤，包含该步骤的所有状态信息
 */
@Data
@AllArgsConstructor
public class SimulationStep {
    private int step;
    private Map<String, String> states;
    private Map<String, String> reasons;
    private List<Map<String, Object>> links;
}
