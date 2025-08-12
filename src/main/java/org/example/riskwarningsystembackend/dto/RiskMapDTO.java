package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RiskMapDTO {
    private String name; // 公司名称
    private List<Object> value; // 风险等级
    private String risk; // 风险等级
}
