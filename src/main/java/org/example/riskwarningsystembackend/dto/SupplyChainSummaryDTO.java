package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupplyChainSummaryDTO {
    private String networkRisk; // 网络风险
    private long highRiskCount; // 高风险公司数
    private long mediumRiskCount; // 中高风险公司数
    private long lowRiskCount; // 低风险公司数
}
