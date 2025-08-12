package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskAnalysisDto {
    private String name; // 公司名称
    private String level; // 风险等级
    private String levelClass; // 风险等级对应的类
    private String reason; // 风险原因
}
