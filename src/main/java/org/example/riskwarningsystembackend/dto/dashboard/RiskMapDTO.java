package org.example.riskwarningsystembackend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 风险地图数据传输对象
 * 用于封装风险地图相关的数据信息
 */
@Data
@AllArgsConstructor
public class RiskMapDTO {
    private String name; // 公司名称
    private List<Object> value; // 风险等级
    private String risk; // 风险等级
}

