package org.example.riskwarningsystembackend.dto.uestc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskStatusOverviewDTO {
    private Integer riskCompanyCount;
    private Integer totalCompanyCount;
    private List<String> riskCompanies;
    private List<String> normalCompanies;
}
