package org.example.riskwarningsystembackend.chainrisk;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class SimulationNode {
    private String id;
    private String status = "正常";
    private double totalNormalImport = 0;
    private double totalNormalExport = 0;
    private Map<String, Double> currentImportValue = new HashMap<>();
    private Map<String, Double> currentExportValue = new HashMap<>();

    public SimulationNode(String id) {
        this.id = id;
    }

    public double getCurrentTotalImport() {
        return this.currentImportValue.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double getCurrentTotalExport() {
        return this.currentExportValue.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}