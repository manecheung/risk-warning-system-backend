package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskDistributionDTO {
    private long value; // 数量
    private String name; // 分类名称
}
