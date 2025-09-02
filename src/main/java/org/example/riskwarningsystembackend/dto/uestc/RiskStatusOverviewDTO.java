package org.example.riskwarningsystembackend.dto.uestc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 风险状态概览DTO
 * 用于接收和解析电子科技大学风险预警系统API返回的风险状态概览数据
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskStatusOverviewDTO {
    private Integer riskCompanyCount;
    private Integer totalCompanyCount;
    private List<String> riskCompanies;
    private List<String> normalCompanies;
}
