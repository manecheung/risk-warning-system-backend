package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyListDTO {
    private Long id; //公司ID
    private String name; // 公司名称
    private String industry; // 所属行业
    private String tech; // 公司技术
    private String finance; // 公司财务
    private String law; // 公司法律
    private String credit; // 公司信用
    private String reason; // 公司风险
}
