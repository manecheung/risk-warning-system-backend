package org.example.riskwarningsystembackend.chainrisk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SimulationResult {
    private List<SimulationStep> log;
    private Map<String, String> abnormalReason;
}