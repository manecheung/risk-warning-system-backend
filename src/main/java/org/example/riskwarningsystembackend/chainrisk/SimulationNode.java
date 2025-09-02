package org.example.riskwarningsystembackend.chainrisk;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 产业链风险模拟节点类
 * 表示产业链中的一个国家或经济体节点，包含其贸易状态和风险信息
 */
@Data
@NoArgsConstructor
public class SimulationNode {
    private String id;
    private String status = "正常";
    private double totalNormalImport = 0;
    private double totalNormalExport = 0;
    private Map<String, Double> currentImportValue = new HashMap<>();
    private Map<String, Double> currentExportValue = new HashMap<>();

    /**
     * 构造函数，创建具有指定ID的模拟节点
     *
     * @param id 节点ID，通常为国家或经济体名称
     */
    public SimulationNode(String id) {
        this.id = id;
    }

    /**
     * 获取当前总进口值
     * 计算当前从所有贸易伙伴进口的总值
     *
     * @return double 当前总进口值
     */
    public double getCurrentTotalImport() {
        return this.currentImportValue.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * 获取当前总出口值
     * 计算当前向所有贸易伙伴出口的总值
     *
     * @return double 当前总出口值
     */
    public double getCurrentTotalExport() {
        return this.currentExportValue.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
