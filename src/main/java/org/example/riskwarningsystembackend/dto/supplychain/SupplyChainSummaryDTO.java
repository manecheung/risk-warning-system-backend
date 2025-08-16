package org.example.riskwarningsystembackend.dto.supplychain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 供应链风险汇总数据传输对象
 * 用于封装供应链网络的整体风险统计信息
 */
@Data
@AllArgsConstructor
public class SupplyChainSummaryDTO {
    private String networkRisk; // 网络风险
    private long highRiskCount; // 高风险公司数
    private long mediumRiskCount; // 中高风险公司数
    private long lowRiskCount; // 低风险公司数
}

