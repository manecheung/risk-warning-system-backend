package org.example.riskwarningsystembackend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 风险分布数据传输对象
 * 用于封装风险分布的统计数据，包括风险数量和分类名称
 */
@Data
@AllArgsConstructor
public class RiskDistributionDTO {
    private long value; // 数量
    private String name; // 分类名称
}

