package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskAnalysisDto {
    private String name;
    private String level;
    private String levelClass;
    private String reason;
}
