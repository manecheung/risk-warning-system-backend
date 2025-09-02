package org.example.riskwarningsystembackend.chainrisk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SimulationStep {
    private int step;
    private Map<String, String> states;
    private Map<String, String> reasons;
    private List<Map<String, Object>> links;
}