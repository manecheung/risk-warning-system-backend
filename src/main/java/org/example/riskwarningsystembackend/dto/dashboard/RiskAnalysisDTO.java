package org.example.riskwarningsystembackend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 风险分析数据传输对象
 * 用于封装公司风险分析的相关信息
 */
@Data
@AllArgsConstructor
public class RiskAnalysisDTO {
    private String name; // 公司名称
    private String level; // 风险等级
    private String levelClass; // 风险等级对应的类
    private String reason; // 风险原因
}

